# ✅ FIX: Giữ màu đã chọn khi chuyển từ None sang item khác

## 📋 VẤN ĐỀ

**Hiện tượng:**
1. Click `btnNone` ở một layer
2. Chọn màu X trong rcvColor (khi đang ở None mode)
3. Click vào item có ảnh khác
4. ❌ **rcvColor bị reset**, không giữ màu X đã chọn

**Nguyên nhân:**
- Khi click None → chọn màu, `positionColorItemList[layer]` được cập nhật ✅
- Nhưng khi click item mới, `handleFillLayer()` **TẠO LẠI colorList** từ đầu
- → Không dùng `positionColorItemList` → Màu bị mất

---

## ✅ GIẢI PHÁP (CHỈ 2 DÒNG CODE)

### **1. CustomizeViewModel.kt - Line 656**

**Thêm logic cập nhật colorItemNavList trong `setClickFillLayer()`:**

```kotlin
suspend fun setClickFillLayer(item: ItemNavCustomModel, position: Int): String {
    // ...existing code...
    
    setIsSelectedItem(positionNavSelected.value)
    setItemNavList(_positionNavSelected.value, position)
    
    // ✅ FIX: Cập nhật colorItemNavList để giữ màu đã chọn khi chuyển từ None sang item khác
    if (item.listImageColor.isNotEmpty()) {
        val currentColorIndex = positionColorItemList.value[positionNavSelected.value]
        val safeColorIndex = currentColorIndex.coerceIn(0, item.listImageColor.size - 1)
        setColorItemNav(positionNavSelected.value, safeColorIndex)
    }
    
    return pathSelected
}
```

**Giải thích:**
- Sau khi set item, cập nhật `colorItemNavList` dựa vào `positionColorItemList` đã lưu
- `setColorItemNav()` sẽ rebuild colorList với màu đúng được highlight

---

### **2. CustomizeActivity.kt - Line 845**

**Xóa đoạn rebuild colorList, chỉ dùng list từ ViewModel:**

```kotlin
private fun handleFillLayer(item: ItemNavCustomModel, position: Int) {
    lifecycleScope.launch(Dispatchers.IO) {
        val pathSelected = viewModel.setClickFillLayer(item, position)
        
        withContext(Dispatchers.Main) {
            renderAllLayers()
            customizeLayerAdapter.submitList(viewModel.itemNavList.value[viewModel.positionNavSelected.value])

            // ✅ FIX: Dùng colorItemNavList từ ViewModel (đã được cập nhật đúng màu trong setClickFillLayer)
            colorLayerAdapter.submitListWithLog(viewModel.colorItemNavList.value[viewModel.positionNavSelected.value])
            
            // Scroll to selected color if needed
            val selectedColorIndex = viewModel.colorItemNavList.value[viewModel.positionNavSelected.value]
                .indexOfFirst { it.isSelected }
            if (selectedColorIndex >= 0) {
                binding.rcvColor.post {
                    binding.rcvColor.smoothScrollToPosition(selectedColorIndex)
                }
            }

            setColorRecyclerViewEnabled(true)
        }
    }
}
```

**Giải thích:**
- ❌ **XÓA**: Đoạn code tạo lại `colorList` bằng tay (17 dòng)
- ✅ **THAY**: Chỉ submit `colorItemNavList` từ ViewModel (đã được cập nhật đúng)
- ✅ **THÊM**: Auto scroll đến màu đã chọn

---

## 🎯 KẾT QUẢ

### **Trước khi fix:**
```
1. Click btnNone
2. Chọn màu 5
3. Click item khác
4. ❌ rcvColor hiển thị màu 0 (hoặc màu random)
```

### **Sau khi fix:**
```
1. Click btnNone
2. Chọn màu 5
3. Click item khác
4. ✅ rcvColor vẫn hiển thị màu 5 (nếu item mới có đủ màu)
5. ✅ Nếu item mới có ít màu hơn → tự động chọn màu cuối cùng (coerce)
```

---

## 📊 FLOW MỚI

```
User clicks btnNone
    ↓
User clicks color #5 in rcvColor
    ↓
handleChangeColorLayer()
    ↓
viewModel.setColorItemNav(layer, 5)  ← Lưu vào positionColorItemList[layer]
    ↓
[positionColorItemList[layer] = 5] ✅ Đã lưu

---

User clicks item khác (có ảnh)
    ↓
handleFillLayer(item)
    ↓
viewModel.setClickFillLayer(item)
    ├─ Lấy currentColorIndex = positionColorItemList[layer] = 5 ✅
    ├─ safeColorIndex = coerceIn(5, 0, item.colors.size-1)
    ├─ setColorItemNav(layer, safeColorIndex)  ← ✅ CẬP NHẬT colorItemNavList
    └─ return pathSelected
    ↓
Activity.handleFillLayer()
    ├─ renderAllLayers()
    └─ colorLayerAdapter.submitList(colorItemNavList[layer])  ← ✅ DÙNG LIST ĐÃ CẬP NHẬT
    ↓
✅ rcvColor hiển thị đúng màu đã chọn
```

---

## 🔍 TESTING

**Test case 1: Item mới có đủ màu**
```
1. Layer Eyes: Item1 (10 màu)
2. Click None → Chọn màu 5
3. Click Item2 (8 màu)
4. ✅ Expected: Màu 5 vẫn được chọn
```

**Test case 2: Item mới có ít màu hơn**
```
1. Layer Eyes: Item1 (10 màu)
2. Click None → Chọn màu 8
3. Click Item2 (5 màu)
4. ✅ Expected: Màu 4 được chọn (index cuối cùng, coerce 8→4)
```

**Test case 3: Chuyển layer khác**
```
1. Layer Eyes: None → Chọn màu 3
2. Chuyển sang Layer Mouth
3. ✅ Expected: Layer Mouth giữ nguyên màu đã chọn trước đó
```

---

## 📝 LƯU Ý

1. **Logic coerce màu:**
   - Nếu item mới có **ít màu hơn** → tự động chọn màu cuối cùng
   - Ví dụ: Đang chọn màu 8, item mới chỉ có 5 màu → chọn màu 4

2. **Không ảnh hưởng:**
   - Logic click màu bình thường (không qua None)
   - Logic random
   - Logic switch navigation tab

3. **Performance:**
   - Giảm 17 dòng code rebuild colorList → tăng performance
   - Chỉ 1 lần rebuild trong ViewModel → consistent

---

## 🚀 COMMIT MESSAGE

```
fix: Giữ màu đã chọn khi chuyển từ None sang item khác

- Thêm logic cập nhật colorItemNavList trong setClickFillLayer()
- Xóa đoạn rebuild colorList trong handleFillLayer()
- Auto scroll đến màu đã chọn sau khi switch item

Fixes: rcvColor bị reset về 0 khi chuyển từ None sang item mới
```

---

**Date:** 2025-11-01  
**Files changed:** 2  
**Lines added:** 9  
**Lines removed:** 17  
**Net change:** -8 lines (cleaner code!)

