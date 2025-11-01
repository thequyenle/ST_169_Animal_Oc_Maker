# 📊 VISUAL FLOW - Giữ màu khi chuyển từ None sang item khác

## 🔴 TRƯỚC KHI FIX (Flow cũ - BỊ LỖI)

```
┌─────────────────────────────────────────────────────────────┐
│ 1. User click btnNone                                       │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ handleNoneLayer()                                           │
│  - setPathSelected(pathIndex, "")                           │
│  - setItemNavList(layer, position)                          │
│  ✅ btnNone.isSelected = true                               │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. User click màu 5 trong rcvColor                         │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ handleChangeColorLayer(5)                                   │
│  - if (item.path == NONE_LAYER):                            │
│      viewModel.setColorItemNav(layer, 5)                    │
│      ✅ positionColorItemList[layer] = 5                    │
│      ✅ colorItemNavList[layer][5].isSelected = true        │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. User click Item A (có 8 màu)                            │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ handleFillLayer(itemA, position)                            │
│  ↓                                                           │
│  viewModel.setClickFillLayer(itemA, position)               │
│    - currentColorIndex = positionColorItemList[layer] = 5   │
│    - safeColorIndex = coerceIn(5, 0, 7) = 5                │
│    - pathSelected = itemA.colors[5].path                    │
│    - setPathSelected(pathIndex, pathSelected)               │
│    ❌ KHÔNG gọi setColorItemNav()                           │
│  ↓                                                           │
│  withContext(Main):                                         │
│    renderAllLayers()                                        │
│    ❌ TẠO MỚI colorList từ itemA.listImageColor             │
│    ❌ safeColorIndex = positionColorItemList[layer]         │
│       = 5 (từ bước 2, KHÔNG ĐÚNG cho itemA)                 │
│    ❌ colorList[5].isSelected = true                        │
│       (Nhưng itemA có thể có colors khác item cũ!)          │
│    colorLayerAdapter.submitList(colorList) ← LIST MỚI       │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ ❌ KẾT QUẢ: Màu bị sai hoặc reset về 0                     │
│ Vì: colorList mới được tạo từ itemA, không match với       │
│     positionColorItemList[layer] = 5 (của item cũ)         │
└─────────────────────────────────────────────────────────────┘
```

---

## 🟢 SAU KHI FIX (Flow mới - ĐÚNG)

```
┌─────────────────────────────────────────────────────────────┐
│ 1. User click btnNone                                       │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ handleNoneLayer()                                           │
│  - setPathSelected(pathIndex, "")                           │
│  - setItemNavList(layer, position)                          │
│  ✅ btnNone.isSelected = true                               │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. User click màu 5 trong rcvColor                         │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ handleChangeColorLayer(5)                                   │
│  - if (item.path == NONE_LAYER):                            │
│      viewModel.setColorItemNav(layer, 5)                    │
│      ✅ positionColorItemList[layer] = 5                    │
│      ✅ colorItemNavList[layer][5].isSelected = true        │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. User click Item A (có 8 màu)                            │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ handleFillLayer(itemA, position)                            │
│  ↓                                                           │
│  viewModel.setClickFillLayer(itemA, position)               │
│    - currentColorIndex = positionColorItemList[layer] = 5   │
│    - safeColorIndex = coerceIn(5, 0, 7) = 5                │
│    - pathSelected = itemA.colors[5].path                    │
│    - setPathSelected(pathIndex, pathSelected)               │
│    ✅ NEW FIX: setColorItemNav(layer, 5)                    │
│       ✅ colorItemNavList[layer] = itemA colors             │
│       ✅ colorItemNavList[layer][5].isSelected = true       │
│  ↓                                                           │
│  withContext(Main):                                         │
│    renderAllLayers()                                        │
│    ✅ DÙNG colorItemNavList từ ViewModel                    │
│       (đã được rebuild đúng ở trên)                         │
│    colorLayerAdapter.submitList(                            │
│        viewModel.colorItemNavList.value[layer]              │
│    )                                                         │
│    ✅ Auto scroll to position 5                             │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ ✅ KẾT QUẢ: Màu 5 được giữ nguyên và highlight đúng        │
│ Vì: colorItemNavList đã được rebuild trong ViewModel        │
│     với itemA.colors và position = 5 đúng                   │
└─────────────────────────────────────────────────────────────┘
```

---

## 📊 DATA STRUCTURE

### **positionColorItemList: ArrayList<Int>**
```
Lưu vị trí màu đã chọn cho MỖI LAYER (navigation tab)

Index = Layer position (positionNavSelected)
Value = Color position đã chọn

[0, 5, 2, 0, 7, ...]
 ↑  ↑  ↑  ↑  ↑
 │  │  │  │  └─ Layer 4 (Hair): đang chọn màu 7
 │  │  │  └──── Layer 3 (Tail): đang chọn màu 0
 │  │  └─────── Layer 2 (Mouth): đang chọn màu 2
 │  └────────── Layer 1 (Eyes): đang chọn màu 5 ✅
 └───────────── Layer 0 (Body): đang chọn màu 0
```

### **colorItemNavList: ArrayList<ArrayList<ItemColorModel>>**
```
Lưu danh sách màu (với trạng thái isSelected) cho MỖI LAYER

colorItemNavList[1] = Layer Eyes colors:
[
  ItemColorModel(color="#FF0000", isSelected=false),  // 0
  ItemColorModel(color="#00FF00", isSelected=false),  // 1
  ItemColorModel(color="#0000FF", isSelected=false),  // 2
  ItemColorModel(color="#FFFF00", isSelected=false),  // 3
  ItemColorModel(color="#FF00FF", isSelected=false),  // 4
  ItemColorModel(color="#00FFFF", isSelected=true),   // 5 ✅
  ItemColorModel(color="#FFFFFF", isSelected=false),  // 6
  ItemColorModel(color="#000000", isSelected=false),  // 7
]
```

---

## 🔄 SO SÁNH TRƯỚC/SAU

### **TRƯỚC:**
```kotlin
// Activity tự tạo colorList mới
val colorList = ArrayList<ItemColorModel>()
item.listImageColor.forEachIndexed { index, colorItem ->
    colorList.add(ItemColorModel(
        color = colorItem.color,
        isSelected = (index == safeColorIndex)  // ❌ SAI LOGIC
    ))
}
colorLayerAdapter.submitList(colorList)
```
**Vấn đề:** 
- `safeColorIndex` từ `positionColorItemList[layer]` (của item CŨ)
- `colorList` tạo từ item MỚI
- → Không khớp → Màu sai

---

### **SAU:**
```kotlin
// ViewModel rebuild colorItemNavList TRƯỚC
if (item.listImageColor.isNotEmpty()) {
    val currentColorIndex = positionColorItemList.value[positionNavSelected.value]
    val safeColorIndex = currentColorIndex.coerceIn(0, item.listImageColor.size - 1)
    setColorItemNav(positionNavSelected.value, safeColorIndex)  // ✅ Rebuild đúng
}

// Activity chỉ submit list từ ViewModel
colorLayerAdapter.submitList(
    viewModel.colorItemNavList.value[layer]  // ✅ List đã đúng
)
```
**Giải pháp:**
- ViewModel rebuild `colorItemNavList[layer]` với item MỚI
- `safeColorIndex` được coerce đúng theo item MỚI
- Activity chỉ submit list (không tạo mới)
- → Màu đúng

---

## 🎯 KEY POINTS

1. **Timing matters:** Rebuild colorList PHẢI ở ViewModel (trước khi Activity submit)
2. **Single source of truth:** colorItemNavList là nguồn dữ liệu duy nhất
3. **Activity không tạo data:** Activity chỉ submit list từ ViewModel
4. **Coerce đúng:** safeColorIndex được tính theo item MỚI, không phải item CŨ

---

**Diagram by:** AI Senior Android Developer  
**Date:** 2025-11-01

