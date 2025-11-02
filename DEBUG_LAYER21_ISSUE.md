# Debug: Layer[21] path được lưu sai vị trí

## Vấn đề phát hiện từ Log

### Click 1: Item "1.png" ở posNav=21
```
positionNavSelected: 21
positionCustom: 0
✅ pathSelected: 1.png

pathSelectedList after click:
  [0] = 1.png     ← Body layer
  [11] = 1.png    ← ⚠️ SAI! Lẽ ra phải là [21]
  [21] = EMPTY    ← ⚠️ SAI! Lẽ ra phải là "1.png"
```

### Click 2: Item "2.png" ở posNav=11  
```
positionNavSelected: 11
positionCustom: 22
✅ pathSelected: 2.png

pathSelectedList after click:
  [0] = 1.png     ← Body layer (từ click 1)
  [11] = 2.png    ← Đúng! Update từ "1.png" → "2.png"
  [21] = EMPTY    ← Vẫn EMPTY
```

## Phân tích

### Điều gì đã xảy ra?

**Click 1:**
1. User click item ở **posNav=21**
2. `setClickFillLayer()` được gọi với `positionNavSelected=21`
3. `getPathIndexForLayer(21)` được gọi
4. **KẾT QUẢ SAI:** Trả về **11** thay vì **21**
5. `setPathSelected(11, "1.png")` → lưu vào `pathSelectedList[11]`
6. **KẾT QUẢ:** Ảnh "1.png" được lưu vào index 11, không phải 21!

**Click 2:**
1. User click item khác ở **posNav=11**
2. `getPathIndexForLayer(11)` trả về **11** (đúng)
3. `setPathSelected(11, "2.png")` → ghi đè `pathSelectedList[11]`
4. **KẾT QUẢ:** Ảnh "1.png" bị mất, thay bằng "2.png"

### Tại sao getPathIndexForLayer(21) trả về 11?

**Nguyên nhân tiềm ẩn:**

1. **Cache bị sai:** 
   ```kotlin
   cache[21] = 11  // ⚠️ SAI! Lẽ ra: cache[21] = 21
   ```

2. **Hardfix không được gọi:**
   - Hardfix ở `getPathIndexForLayer()` check `positionSelected == 1`
   - Nhưng có thể `positionSelected` không phải 1 (Miley)?
   - Hoặc Layer[21] trong data có posNav khác 20?

3. **Logic fallback sai:**
   ```kotlin
   val actualPositionNav = when (positionNavigation) {
       21 -> {
           val layer22 = layerList.find { it.positionCustom == 22 }
           val layer22Index = layerList.indexOf(layer22)
           return layer22Index  // ← Có thể trả về 11?
       }
   }
   ```

## Cần kiểm tra

### 1. Log cache khi build
Cần xem log:
```
📋 Layer Index Cache built: X entries (Character 1)
   posNav=21 → Layer[??] (posCus=??)
```

### 2. Log getPathIndexForLayer(21)
Cần xem:
- `positionSelected` = ?
- Hardfix có được trigger không?
- Cache lookup trả về gì?

### 3. Kiểm tra layerList structure
- Layer[21] thực sự có posNav=21 không?
- Hay posNav=20 (như log trước)?
- Layer nào có positionCustom=22?

## Giải pháp tạm thời

**Thêm log chi tiết vào `getPathIndexForLayer()`:**
```kotlin
fun getPathIndexForLayer(positionNavigation: Int): Int {
    Log.d("CustomizeViewModel", "🔍 getPathIndexForLayer($positionNavigation)")
    Log.d("CustomizeViewModel", "   positionSelected: $positionSelected")
    
    // ...existing logic...
    
    Log.d("CustomizeViewModel", "   → Returning: $layerIndex")
    return layerIndex
}
```

## Ngày phát hiện
31/10/2025

