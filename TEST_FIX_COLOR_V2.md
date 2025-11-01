# 🎯 HƯỚNG DẪN TEST - Fix giữ màu (Lần 2 - ĐÚNG)

## ⚠️ LẦN FIX ĐẦU THẤT BẠI - ĐÃ SỬA LẠI

### **Vấn đề lần 1:**
- Chỉ gọi `setColorItemNav()` → CHỈ update `isSelected`, KHÔNG rebuild list màu
- `colorItemNavList` vẫn chứa màu của item CŨ
- Màu index 5 của item A ≠ màu index 5 của item B

### **Fix lần 2 (ĐÚNG):**
- **REBUILD** `colorItemNavList` từ `item.listImageColor` (màu của item MỚI)
- Thay thế hoàn toàn list cũ
- Màu được giữ theo INDEX, không theo giá trị màu cũ

---

## 📝 THAY ĐỔI CODE

### **1. CustomizeViewModel.kt**

#### **Thêm function mới (Line 189):**
```kotlin
fun setPositionColorForLayer(layerPosition: Int, colorPosition: Int) {
    _positionColorItemList.value[layerPosition] = colorPosition
    Log.d("CustomizeViewModel", "🎨 setPositionColorForLayer: layer=$layerPosition, color=$colorPosition")
}
```

#### **Sửa setClickFillLayer (Line 656):**
```kotlin
// ✅ THAY ĐỔI: Rebuild colorItemNavList từ item mới
if (item.listImageColor.isNotEmpty()) {
    val currentColorIndex = positionColorItemList.value[positionNavSelected.value]
    val safeColorIndex = currentColorIndex.coerceIn(0, item.listImageColor.size - 1)
    
    // ✅ TẠO LIST MỚI (không dùng setColorItemNav)
    val newColorList = ArrayList<ItemColorModel>()
    item.listImageColor.forEachIndexed { index, colorItem ->
        newColorList.add(ItemColorModel(
            color = colorItem.color,  // Màu từ item MỚI
            isSelected = (index == safeColorIndex)
        ))
    }
    _colorItemNavList.value[positionNavSelected.value] = newColorList
    
    if (currentColorIndex != safeColorIndex) {
        _positionColorItemList.value[positionNavSelected.value] = safeColorIndex
    }
    
    Log.d("CustomizeViewModel", "🎨 Rebuilt colorItemNavList: ${newColorList.size} colors, selected=$safeColorIndex")
} else {
    _colorItemNavList.value[positionNavSelected.value] = arrayListOf()
}
```

### **2. CustomizeActivity.kt**

#### **Sửa handleChangeColorLayer (Line 1095):**
```kotlin
if (currentSelectedItem?.path == AssetsKey.NONE_LAYER) {
    lifecycleScope.launch(Dispatchers.IO) {
        viewModel.setPositionColorForLayer(viewModel.positionNavSelected.value, position)
        withContext(Dispatchers.Main) {
            viewModel.setColorItemNav(viewModel.positionNavSelected.value, position)
            colorLayerAdapter.submitListWithLog(viewModel.colorItemNavList.value[viewModel.positionNavSelected.value])
            Log.d("CustomizeActivity", "🎨 Color selected in None mode (position=$position) - Will apply when item selected")
        }
    }
    return@launch
}
```

---

## 🧪 TEST STEPS

### **Test 1: None → Màu → Item khác**
```
1. Mở app → Vào Customize
2. Chọn layer Eyes (hoặc bất kỳ layer nào)
3. Click btnNone (nút đầu tiên)
4. Scroll rcvColor, click màu index 5 (màu thứ 6)
5. Click vào item có ảnh bất kỳ

Expected:
✅ Item được load ra
✅ rcvColor hiển thị màu index 5 của item MỚI được highlight
✅ Character render với item + màu index 5

Log kiểm tra:
Filter Logcat: "CustomizeViewModel"
Tìm: "🎨 Rebuilt colorItemNavList"
Expected log: "🎨 Rebuilt colorItemNavList: X colors, selected=5"
```

### **Test 2: Màu cao → Item ít màu**
```
1. Click None
2. Click màu index 8
3. Click item chỉ có 5 màu (index 0-4)

Expected:
✅ Màu index 4 được chọn (coerce 8→4)
✅ Log: "🎨 Rebuilt colorItemNavList: 5 colors, selected=4"
```

### **Test 3: Chuyển item nhiều lần**
```
1. Click Item A
2. Click None → Màu 3
3. Click Item B
4. Click None → Màu 7
5. Click Item C

Expected:
✅ Item C hiển thị màu index 7
✅ Mỗi lần click item mới → log "🎨 Rebuilt colorItemNavList"
```

---

## 🔍 DEBUG

### **Nếu vẫn không giữ được màu:**

**1. Check log:**
```
Filter: CustomizeViewModel
Keyword: "🎨 Rebuilt colorItemNavList"
```

**Expected:**
```
📍 setClickFillLayer:
   positionNavSelected: 1
   pathIndex returned: 3
🎨 Rebuilt colorItemNavList: 8 colors, selected=5
✅ SAVED: pathSelectedList[3] = xxx.png
```

**Nếu KHÔNG thấy log "🎨 Rebuilt":**
- Code chưa được build đúng
- Hoặc vào nhánh else (item.listImageColor.isEmpty())

**2. Breakpoint:**
- File: `CustomizeViewModel.kt`
- Line: 665 (trong setClickFillLayer, đoạn rebuild)
- Check: `item.listImageColor.size`, `safeColorIndex`, `newColorList.size`

**3. Check data:**
```kotlin
// Thêm log trong handleFillLayer (Activity):
Log.d("TEST", "Before click item:")
Log.d("TEST", "  positionColorItemList[${viewModel.positionNavSelected.value}] = ${viewModel.positionColorItemList.value[viewModel.positionNavSelected.value]}")
Log.d("TEST", "  item.listImageColor.size = ${item.listImageColor.size}")

// Sau khi setClickFillLayer:
Log.d("TEST", "After setClickFillLayer:")
Log.d("TEST", "  colorItemNavList[${viewModel.positionNavSelected.value}].size = ${viewModel.colorItemNavList.value[viewModel.positionNavSelected.value].size}")
Log.d("TEST", "  selected index = ${viewModel.colorItemNavList.value[viewModel.positionNavSelected.value].indexOfFirst { it.isSelected }}")
```

---

## 📊 ĐIỂM KHÁC BIỆT

### **Fix Lần 1 (SAI):**
```kotlin
if (item.listImageColor.isNotEmpty()) {
    setColorItemNav(layer, safeColorIndex)
    // ❌ Chỉ update isSelected trong list CŨ
    // ❌ colorItemNavList vẫn chứa màu của item trước đó
}
```

### **Fix Lần 2 (ĐÚNG):**
```kotlin
if (item.listImageColor.isNotEmpty()) {
    val newColorList = ArrayList<ItemColorModel>()
    item.listImageColor.forEachIndexed { index, colorItem ->
        newColorList.add(ItemColorModel(
            color = colorItem.color,  // ✅ Màu từ item MỚI
            isSelected = (index == safeColorIndex)
        ))
    }
    _colorItemNavList.value[layer] = newColorList  // ✅ Thay thế list CŨ
}
```

---

## ✅ CHECKLIST

- [ ] Code đã build thành công
- [ ] Test Case 1: None → Màu → Item khác ✅
- [ ] Test Case 2: Màu cao → Item ít màu ✅
- [ ] Test Case 3: Chuyển item nhiều lần ✅
- [ ] Log "🎨 Rebuilt colorItemNavList" xuất hiện đúng
- [ ] rcvColor hiển thị màu đúng sau khi switch item
- [ ] Character render đúng với màu đã chọn
- [ ] Không crash, không lag

---

## 📱 KẾT QUẢ MONG ĐỢI

**Scenario:**
```
1. Click None
2. Chọn màu Pink (index 5)
3. Click Item A (có 10 màu, Pink không phải là màu index 5)
```

**Trước Fix 2:**
```
❌ rcvColor hiển thị màu Pink (màu cũ)
❌ Character render sai màu
```

**Sau Fix 2:**
```
✅ rcvColor hiển thị màu tại INDEX 5 của Item A (không phải Pink)
✅ Character render đúng với màu index 5 của Item A
✅ Log: "🎨 Rebuilt colorItemNavList: 10 colors, selected=5"
```

---

**Test Date:** 2025-11-01  
**Version:** Fix Lần 2 (Cuối cùng)  
**Status:** ⏳ Cần test trên device

