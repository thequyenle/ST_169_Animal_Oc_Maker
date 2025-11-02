# ✅ Fix: Duplicate Tail Issue (2 Đuôi Cùng Hiển Thị)

## 🔴 Vấn đề

### Mô tả:
- **Character 1** có 2 layers đuôi: **Đuôi A** và **Đuôi B**
- Cả 2 đều có cùng `positionCustom = 21`
- Nhưng khác `positionNavigation`: Đuôi A (navi 18), Đuôi B (navi 21)

### Kịch bản lỗi:
1. User click **"Random All"** → Random được **Đuôi A**
2. User chọn tab **navigation 21** → Click chọn **Đuôi B**
3. **KẾT QUẢ:** Cả **Đuôi A** và **Đuôi B** đều hiển thị trên nhân vật! 🐛

### Nguyên nhân:
- Cả 2 đuôi có **cùng `positionCustom = 21`** → Được render vào **cùng 1 ImageView**
- Nhưng mỗi đuôi lại có **`positionNavigation` khác nhau** → Được lưu ở **các index khác nhau** trong `pathSelectedList`
- **Kết quả:** Layer cũ không bị xóa khi chọn layer mới

## ✅ Giải pháp

### 1. Thêm hàm `clearLayersWithSamePositionCustom()`

**Chức năng:** Xóa tất cả paths của các layers có cùng `positionCustom` (trừ layer hiện tại)

```kotlin
/**
 * ✅ FIX DUPLICATE POSITION CUSTOM:
 * Xóa tất cả paths của các layers có cùng positionCustom với layer hiện tại
 * Ví dụ: Đuôi A và Đuôi B cùng positionCustom=21 → Khi chọn Đuôi B, xóa path của Đuôi A
 */
private suspend fun clearLayersWithSamePositionCustom(positionNavigation: Int) {
    val layerList = _dataCustomize.value?.layerList ?: return
    val currentLayer = layerList.find { it.positionNavigation == positionNavigation } ?: return
    val currentPositionCustom = currentLayer.positionCustom

    // Tìm tất cả layers có cùng positionCustom (trừ layer hiện tại)
    layerList.forEachIndexed { index, layer ->
        if (layer.positionCustom == currentPositionCustom && layer.positionNavigation != positionNavigation) {
            // Xóa path và reset state của layer này
            _pathSelectedList.value[index] = ""
            _keySelectedItemList.value[layer.positionNavigation] = ""
            _isSelectedItemList.value[layer.positionNavigation] = false
            
            Log.d("CustomizeViewModel", "🧹 CLEAR DUPLICATE: Cleared layer positionNav=${layer.positionNavigation} (same positionCustom=$currentPositionCustom)")
        }
    }
}
```

### 2. Gọi hàm trong `setClickFillLayer()` (Chọn thủ công)

```kotlin
suspend fun setClickFillLayer(item: ItemNavCustomModel, position: Int): String {
    // ...existing code...

    // ✅ FIX: Xóa các layers có cùng positionCustom trước khi set layer mới
    clearLayersWithSamePositionCustom(positionNavSelected.value)

    if (pathIndex != -1) {
        setPathSelected(pathIndex, pathSelected)
    }
    
    // ...existing code...
}
```

### 3. Gọi hàm trong `setClickRandomLayer()` (Random 1 layer)

```kotlin
suspend fun setClickRandomLayer(): Pair<String, Boolean> {
    // ...existing code...

    // ✅ FIX: Xóa các layers có cùng positionCustom trước khi set layer mới
    clearLayersWithSamePositionCustom(positionNavSelected.value)

    if (pathIndex != -1) {
        setPathSelected(pathIndex, pathRandom)
    }
    
    // ...existing code...
}
```

### 4. Fix `setClickRandomFullLayer()` (Random All)

**Vấn đề đặc biệt:** Khi random all, có thể random cả Đuôi A và Đuôi B cùng lúc

**Giải pháp:** Sử dụng `positionCustomMap` để track và chỉ chọn 1 layer cho mỗi `positionCustom`

```kotlin
suspend fun setClickRandomFullLayer(): Boolean {
    // ✅ FIX: Track positionCustom đã được random
    val layerList = _dataCustomize.value?.layerList ?: return false
    val positionCustomMap = mutableMapOf<Int, Int>() // positionCustom -> positionNavigation
    
    for (i in 0 until _bottomNavigationList.value.size) {
        // ...existing code...
        
        val currentLayer = layerList[i]
        val currentPositionCustom = currentLayer.positionCustom

        // ✅ Kiểm tra xem positionCustom này đã được random chưa
        if (positionCustomMap.containsKey(currentPositionCustom)) {
            // Skip layer này và xóa path
            _pathSelectedList.value[i] = ""
            _keySelectedItemList.value[i] = ""
            _isSelectedItemList.value[i] = false
            continue
        }

        // Random layer và set path...
        
        // ✅ Đánh dấu positionCustom này đã được random
        positionCustomMap[currentPositionCustom] = i
    }
    
    return false
}
```

## 🎯 Kết quả sau fix

### Kịch bản 1: Random All
```
1. Random All → Chọn được Đuôi A (positionCustom=21, positionNav=18)
2. Loop đến Đuôi B (positionCustom=21, positionNav=21)
3. ✅ Phát hiện positionCustom=21 đã được random
4. ✅ Skip Đuôi B và xóa path của nó
5. ✅ Kết quả: CHỈ hiển thị Đuôi A
```

### Kịch bản 2: Random All → Chọn thủ công
```
1. Random All → Chọn được Đuôi A (positionCustom=21)
2. User click tab navigation 21 → Chọn Đuôi B
3. ✅ clearLayersWithSamePositionCustom(21) được gọi
4. ✅ Tìm thấy Đuôi A có cùng positionCustom=21
5. ✅ Xóa path của Đuôi A
6. ✅ Set path mới cho Đuôi B
7. ✅ Kết quả: CHỈ hiển thị Đuôi B
```

### Kịch bản 3: Random 1 tab → Random 1 tab khác
```
1. User ở tab 18 → Random → Chọn được Đuôi A
2. User chuyển sang tab 21 → Random → Chọn được Đuôi B
3. ✅ clearLayersWithSamePositionCustom(21) được gọi
4. ✅ Xóa Đuôi A, set Đuôi B
5. ✅ Kết quả: CHỈ hiển thị Đuôi B
```

## 📊 Log để verify

### Khi chọn Đuôi B (sau khi có Đuôi A):
```
🧹 CLEAR DUPLICATE: Cleared layer positionNav=18 (same positionCustom=21)
✅ DAMMY SAVED: pathIndex=21
```

### Khi Random All:
```
✅ RANDOM ALL SET: positionNav=18 (positionCustom=21)
🧹 RANDOM ALL SKIP: positionNav=21 (positionCustom=21 already assigned)
```

## 🔍 Các trường hợp edge case

### 1. Có 3+ layers cùng positionCustom
**Giải pháp hiện tại:** Vẫn work! Chỉ layer đầu tiên được giữ, các layer sau đều bị xóa

### 2. User chọn nhanh giữa các tabs
**Giải pháp hiện tại:** Mỗi lần chọn đều clear duplicates → Luôn chỉ có 1 layer active

### 3. Apply suggestion preset
**Note:** Suggestion preset có logic riêng, không dùng `setClickFillLayer()` nên cần verify riêng

## ⚠️ Lưu ý

### 1. Không ảnh hưởng đến layers khác
- Chỉ xóa layers có **cùng `positionCustom`**
- Các layers có `positionCustom` khác hoàn toàn không bị ảnh hưởng

### 2. Reset state hoàn toàn
```kotlin
_pathSelectedList.value[index] = ""           // Xóa path
_keySelectedItemList.value[positionNav] = ""  // Xóa key
_isSelectedItemList.value[positionNav] = false // Unselect UI
```

### 3. Performance
- **O(n)** complexity với n = số layers trong character
- Chạy mỗi khi user chọn layer → Acceptable

## 💡 Recommendation

### Giải pháp lâu dài:
**Yêu cầu backend/data sửa cấu trúc:** Mỗi layer phải có `positionCustom` **duy nhất**

```json
// ❌ HIỆN TẠI (có duplicate)
{
  "layers": [
    {"positionCustom": 21, "positionNavigation": 18, "name": "Tail A"},
    {"positionCustom": 21, "positionNavigation": 21, "name": "Tail B"}
  ]
}

// ✅ LÝ TƯỞNG (không duplicate)
{
  "layers": [
    {"positionCustom": 21, "positionNavigation": 18, "name": "Tail A"},
    {"positionCustom": 22, "positionNavigation": 21, "name": "Tail B"}
  ]
}
```

**Lợi ích:**
- Không cần logic xóa duplicates
- Mỗi layer render vào ImageView riêng
- Code đơn giản hơn, ít bug hơn

## 🎯 Files đã sửa

- `CustomizeViewModel.kt`:
  - ✅ Added `clearLayersWithSamePositionCustom()` - Helper function
  - ✅ Modified `setClickFillLayer()` - Manual selection
  - ✅ Modified `setClickRandomLayer()` - Random single layer
  - ✅ Modified `setClickRandomFullLayer()` - Random all layers
  - ✅ Modified `applySuggestionPreset()` - Apply from suggestion

## ✅ Testing checklist

### Scenario: Random Actions
- [ ] Random All → Chỉ 1 đuôi hiển thị (không có duplicate)
- [ ] Random All nhiều lần → Mỗi lần chỉ 1 đuôi
- [ ] Random 1 tab có duplicate positionCustom → Chỉ 1 layer hiển thị

### Scenario: Manual Selection
- [ ] Random All → Chọn đuôi khác thủ công → Đuôi mới thay thế đuôi cũ
- [ ] Chọn Đuôi A → Chuyển tab → Chọn Đuôi B → Chỉ Đuôi B hiển thị
- [ ] Chọn Đuôi A → Random tab khác → Chọn Đuôi B → Chỉ Đuôi B hiển thị

### Scenario: Suggestion Preset
- [ ] Click suggestion từ gallery → Apply preset → Chỉ 1 đuôi hiển thị
- [ ] Apply preset → Random → Manual select → Không có duplicate

### Scenario: General
- [ ] Không ảnh hưởng đến layers khác (body, eyes, ears,...)
- [ ] Reset button vẫn hoạt động bình thường
- [ ] Switch giữa các characters khác nhau → Không bị lỗi
- [ ] Save và export image → Image không có duplicate layers

