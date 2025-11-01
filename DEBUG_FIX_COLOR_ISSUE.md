# 🐛 DEBUG: Vấn đề giữ màu khi chuyển từ None sang item khác

## ❌ VẤN ĐỀ PHÁT HIỆN

**Hiện tượng:** Click None → Chọn màu 5 → Click item khác → Màu vẫn KHÔNG được giữ

## 🔍 PHÂN TÍCH ROOT CAUSE

### **Vấn đề 1: colorItemNavList không đồng bộ với item hiện tại**

```kotlin
// FLOW CŨ (SAI):
1. User click Item A (8 màu)
   → colorItemNavList[layer] = [màu 0, màu 1, ..., màu 7] của Item A

2. User click None
   → colorItemNavList[layer] VẪN = [màu 0, ..., màu 7] của Item A (KHÔNG ĐỔI)
   → itemNavList[layer].isSelected = true (btnNone)

3. User click màu 5 trong rcvColor
   → setColorItemNav(layer, 5)
   → colorItemNavList[layer][5].isSelected = true (màu của Item A)
   → positionColorItemList[layer] = 5 ✅

4. User click Item B (10 màu, KHÁC Item A)
   → setClickFillLayer(Item B)
   → Rebuild colorItemNavList[layer] = [màu 0, ..., màu 9] của Item B
   → Set isSelected tại position=5
   → ❌ NHƯNG màu index 5 của Item B KHÁC màu index 5 của Item A
   → User thấy "màu đổi" (thực ra là đúng, nhưng không phải màu đã chọn lúc ở None)
```

### **Vấn đề 2: Màu hiển thị ở None mode không chính xác**

Khi ở None mode:
- `colorItemNavList[layer]` chứa màu của **item trước đó** (không phải item hiện tại)
- User thấy và chọn màu từ list này
- Nhưng khi click item mới, màu index 5 của item MỚI khác màu đã chọn

**Ví dụ cụ thể:**
```
Item A colors:
[0] = #FF0000 (Red)
[1] = #00FF00 (Green)
[2] = #0000FF (Blue)
...
[5] = #FFFF00 (Yellow) ← User chọn màu này ở None mode

Item B colors:
[0] = #FF00FF (Magenta)
[1] = #00FFFF (Cyan)
[2] = #FFFFFF (White)
...
[5] = #000000 (Black) ← Khi click Item B, màu này được chọn (index 5)

→ User mong đợi Yellow (#FFFF00) nhưng nhận được Black (#000000)
```

---

## ✅ GIẢI PHÁP MỚI

### **Fix 1: Rebuild colorItemNavList trong setClickFillLayer**

```kotlin
// CustomizeViewModel.kt - setClickFillLayer()

if (item.listImageColor.isNotEmpty()) {
    val currentColorIndex = positionColorItemList.value[positionNavSelected.value]
    val safeColorIndex = currentColorIndex.coerceIn(0, item.listImageColor.size - 1)
    
    // ✅ REBUILD colorList từ item MỚI
    val newColorList = ArrayList<ItemColorModel>()
    item.listImageColor.forEachIndexed { index, colorItem ->
        newColorList.add(ItemColorModel(
            color = colorItem.color,
            isSelected = (index == safeColorIndex)
        ))
    }
    _colorItemNavList.value[positionNavSelected.value] = newColorList
    
    // ✅ Cập nhật positionColorItemList
    if (currentColorIndex != safeColorIndex) {
        _positionColorItemList.value[positionNavSelected.value] = safeColorIndex
    }
}
```

**Giải thích:**
- Khi click item mới, REBUILD toàn bộ `colorItemNavList[layer]` từ màu của item MỚI
- Không dùng lại list cũ (có màu của item cũ)
- Set `isSelected` cho màu tại index đã lưu trong `positionColorItemList`

### **Fix 2: Khi ở None mode, chỉ lưu position**

```kotlin
// CustomizeActivity.kt - handleChangeColorLayer()

if (currentSelectedItem?.path == AssetsKey.NONE_LAYER) {
    // ✅ CHỈ lưu positionColorItemList, KHÔNG update colorItemNavList
    lifecycleScope.launch(Dispatchers.IO) {
        viewModel.setPositionColorForLayer(viewModel.positionNavSelected.value, position)
        withContext(Dispatchers.Main) {
            // Update UI để hiển thị màu được chọn
            viewModel.setColorItemNav(viewModel.positionNavSelected.value, position)
            colorLayerAdapter.submitListWithLog(...)
        }
    }
    return@launch
}
```

**Giải thích:**
- Khi ở None mode, CHỈ lưu `positionColorItemList[layer] = position`
- Update `colorItemNavList` để UI hiển thị màu được chọn (visual feedback)
- Nhưng biết rằng list này chứa màu của item CŨ (không chính xác)
- Khi click item MỚI, sẽ rebuild lại list với màu đúng

---

## 🎯 FLOW MỚI (ĐÚNG)

```
1. User click Item A (8 màu)
   → colorItemNavList[layer] = [8 màu của Item A]
   → positionColorItemList[layer] = 0 (default)

2. User click None
   → itemNavList[layer][0].isSelected = true (btnNone)
   → pathSelectedList[pathIndex] = "" (clear)
   → colorItemNavList[layer] VẪN = [8 màu của Item A] (không đổi, OK)

3. User click màu 5 trong rcvColor (lúc này hiển thị màu của Item A)
   → setPositionColorForLayer(layer, 5)
   → positionColorItemList[layer] = 5 ✅
   → setColorItemNav(layer, 5) → colorItemNavList[layer][5].isSelected = true
   → User thấy màu 5 của Item A được highlight

4. User click Item B (10 màu)
   → setClickFillLayer(Item B)
   → currentColorIndex = positionColorItemList[layer] = 5 ✅
   → safeColorIndex = coerceIn(5, 0, 9) = 5
   
   → ✅ REBUILD colorItemNavList[layer]:
      newColorList = [10 màu của Item B]
      newColorList[5].isSelected = true
   → _colorItemNavList.value[layer] = newColorList
   
   → Activity submit newColorList
   → User thấy màu 5 của Item B được chọn ✅

5. Character render với Item B + màu index 5 của Item B ✅
```

---

## 📊 SO SÁNH

### **Trước Fix:**
```
positionColorItemList[layer] = 5 ← Chỉ lưu INDEX
colorItemNavList[layer] = [màu của Item A] ← List CŨ không đổi

→ Khi click Item B:
  - Dùng lại list cũ (màu của Item A)
  - Set isSelected tại index 5
  - ❌ Màu không đúng
```

### **Sau Fix:**
```
positionColorItemList[layer] = 5 ← Lưu INDEX
colorItemNavList[layer] = [màu của Item A] ← List CŨ

→ Khi click Item B:
  - REBUILD list mới (màu của Item B)
  - Set isSelected tại index 5
  - ✅ Màu đúng (index 5 của Item B)
```

---

## 🧪 TEST CASE

### **Test 1: None → Chọn màu → Item khác**
```
1. Click Item A (màu [Red, Green, Blue, Yellow, ...])
2. Click None
3. Click màu Yellow (index 5)
4. Click Item B (màu [Magenta, Cyan, White, Black, Orange, ...])

Expected:
✅ Item B được load
✅ rcvColor hiển thị màu Orange (index 5 của Item B)
✅ Character render với Item B + màu Orange

KHÔNG PHẢI:
❌ rcvColor hiển thị màu Yellow (index 5 của Item A)
```

### **Test 2: Chọn màu cao → Item có ít màu**
```
1. Click Item A (10 màu)
2. Click None
3. Click màu index 8
4. Click Item B (chỉ có 5 màu)

Expected:
✅ safeColorIndex = coerceIn(8, 0, 4) = 4
✅ Màu index 4 của Item B được chọn
```

### **Test 3: None → Chọn màu → None lại → Chọn màu khác → Item**
```
1. Click None
2. Click màu 3
3. Click None lại
4. Click màu 7
5. Click Item A

Expected:
✅ Màu index 7 của Item A được chọn (lần chọn cuối cùng)
```

---

## 📝 KEY CHANGES

### **File 1: CustomizeViewModel.kt**

**Line 186 - Thêm function mới:**
```kotlin
fun setPositionColorForLayer(layerPosition: Int, colorPosition: Int) {
    _positionColorItemList.value[layerPosition] = colorPosition
}
```

**Line 656 - Rebuild colorItemNavList:**
```kotlin
// Trong setClickFillLayer():
if (item.listImageColor.isNotEmpty()) {
    val currentColorIndex = positionColorItemList.value[positionNavSelected.value]
    val safeColorIndex = currentColorIndex.coerceIn(0, item.listImageColor.size - 1)
    
    // ✅ REBUILD colorList từ item MỚI
    val newColorList = ArrayList<ItemColorModel>()
    item.listImageColor.forEachIndexed { index, colorItem ->
        newColorList.add(ItemColorModel(
            color = colorItem.color,
            isSelected = (index == safeColorIndex)
        ))
    }
    _colorItemNavList.value[positionNavSelected.value] = newColorList
    
    if (currentColorIndex != safeColorIndex) {
        _positionColorItemList.value[positionNavSelected.value] = safeColorIndex
    }
} else {
    _colorItemNavList.value[positionNavSelected.value] = arrayListOf()
}
```

### **File 2: CustomizeActivity.kt**

**Line 1095 - Sửa logic handleChangeColorLayer:**
```kotlin
if (currentSelectedItem?.path == AssetsKey.NONE_LAYER) {
    lifecycleScope.launch(Dispatchers.IO) {
        viewModel.setPositionColorForLayer(viewModel.positionNavSelected.value, position)
        withContext(Dispatchers.Main) {
            viewModel.setColorItemNav(viewModel.positionNavSelected.value, position)
            colorLayerAdapter.submitListWithLog(...)
        }
    }
    return@launch
}
```

---

## 🎯 ĐIỂM THEN CHỐT

1. **colorItemNavList PHẢI được rebuild** khi click item mới
2. **Không dùng lại list cũ** (có màu của item cũ)
3. **positionColorItemList lưu INDEX**, không lưu màu thực tế
4. **INDEX chỉ có nghĩa khi biết item nào** (index 5 của Item A ≠ index 5 của Item B)

---

**Debug Date:** 2025-11-01  
**Issue:** Màu không được giữ khi chuyển từ None sang item khác  
**Root Cause:** colorItemNavList không đồng bộ với item hiện tại  
**Solution:** Rebuild colorItemNavList từ item mới trong setClickFillLayer

