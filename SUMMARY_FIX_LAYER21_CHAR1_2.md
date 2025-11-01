# Summary: Fix Layer[21] cho Character 1 & 2

## Vấn đề
- **Character 1 (Miley)** và **Character 2 (Dammy)**: Layer[21] có `posNav=20` trong JSON data
- Khi click item ở posNav=21, path được lưu vào `pathSelectedList[11]` thay vì `[21]`
- Gây ra conflict và layer không render đúng

## Root Cause
`getPathIndexForLayer(21)` trả về **11** thay vì **21** vì:
- Layer[21] có `posNav=20` trong data (duplicate với Layer[20])
- Cache không có entry cho `posNav=21`
- Logic fallback tìm layer có `positionCustom=22` → trả về Layer[11]

## Giải pháp: Hardfix

### 1. buildLayerIndexCache()
```kotlin
// Character 1 & 2: Map Layer[21] vào cache với key=21
if ((positionSelected == 1 || positionSelected == 2) && index == 21 && layer.positionNavigation == 20) {
    cache[21] = index
    return@forEachIndexed
}
```

### 2. getPathIndexForLayer()
```kotlin
// Character 1 & 2: Return 21 trực tiếp khi request posNav=21
if ((positionSelected == 1 || positionSelected == 2) && positionNavigation == 21) {
    return 21
}
```

## Kết quả
✅ Click item ở posNav=21 → path lưu vào `pathSelectedList[21]` (ĐÚNG)
✅ Layer[21] render đúng vào ImageView[positionCustom]
✅ Không còn conflict với Layer[11]
✅ Character 0 không bị ảnh hưởng

## Files đã sửa
1. `CustomizeViewModel.kt`:
   - `buildLayerIndexCache()`: Thêm hardfix mapping
   - `getPathIndexForLayer()`: Thêm hardfix return 21

2. `FIX_HARDCODED_LAYER21_MILEY.md`: Cập nhật documentation

## Scope
- ✅ Character 1 (Miley) - positionSelected == 1
- ✅ Character 2 (Dammy) - positionSelected == 2
- ✅ Layer[21] only
- ❌ Không ảnh hưởng Character 0 hoặc layer khác

## Ngày fix
31/01/2025

