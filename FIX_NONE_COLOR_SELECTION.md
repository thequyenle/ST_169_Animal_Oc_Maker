# Fix: Cho phép select màu khi chọn btnNone

## Vấn đề
Khi người dùng chọn **btnNone** trong màn customize, họ không thể:
- Di chuyển (scroll) trong danh sách màu
- Select (chọn) item màu
- Tương tác với color picker

## Nguyên nhân
- Khi click `btnNone`, hệ thống gọi `setColorRecyclerViewEnabled(false)` để disable color picker
- Function `handleChangeColorLayer` có check `if (!isColorEnabled) return` để ngăn việc select màu
- `ColorLayerAdapter` cũng có check `if (isEnabled)` trước khi cho phép click

## Giải pháp

### 1. CustomizeActivity.kt

#### a) Xóa disable color picker khi click None
**Trước:**
```kotlin
private fun handleNoneLayer(position: Int) {
    // ...
    withContext(Dispatchers.Main) {
        renderAllLayers()
        customizeLayerAdapter.submitList(...)
        setColorRecyclerViewEnabled(false) // ❌ Disable color picker
    }
}
```

**Sau:**
```kotlin
private fun handleNoneLayer(position: Int) {
    // ...
    withContext(Dispatchers.Main) {
        renderAllLayers()
        customizeLayerAdapter.submitList(...)
        // ✅ Vẫn enable color picker để user có thể scroll và select màu
    }
}
```

#### b) Chỉ select màu trong UI, không apply khi đang ở None
**Trước:**
```kotlin
private fun handleChangeColorLayer(position: Int) {
    if (!isColorEnabled) return // ❌ Block việc select màu
    
    lifecycleScope.launch(Dispatchers.IO) {
        val pathColor = viewModel.setClickChangeColor(position)
        // ...
    }
}
```

**Sau:**
```kotlin
private fun handleChangeColorLayer(position: Int) {
    lifecycleScope.launch(Dispatchers.IO) {
        // ✅ Kiểm tra nếu đang ở trạng thái None
        val currentSelectedItem = viewModel.itemNavList.value[viewModel.positionNavSelected.value]
            .firstOrNull { it.isSelected }
        
        if (currentSelectedItem?.path == AssetsKey.NONE_LAYER) {
            // ✅ CHỈ update UI để hiển thị màu được chọn, KHÔNG apply màu lên character
            viewModel.setColorItemNav(viewModel.positionNavSelected.value, position)
            withContext(Dispatchers.Main) {
                // Chỉ cập nhật color adapter để highlight màu được chọn
                colorLayerAdapter.submitListWithLog(viewModel.colorItemNavList.value[viewModel.positionNavSelected.value])
            }
            return@launch
        }
        
        // ✅ Nếu KHÔNG phải None, apply màu bình thường
        val pathColor = viewModel.setClickChangeColor(position)
        // ...
    }
}
```

#### c) Luôn enable color picker
**Trước:**
```kotlin
private fun setColorRecyclerViewEnabled(enabled: Boolean) {
    isColorEnabled = enabled
    binding.rcvColor.alpha = if (enabled) 1.0f else 0.5f // ❌ Thay đổi alpha
    colorLayerAdapter.isEnabled = enabled // ❌ Disable adapter
}
```

**Sau:**
```kotlin
private fun setColorRecyclerViewEnabled(enabled: Boolean) {
    isColorEnabled = enabled
    binding.rcvColor.alpha = 1.0f // ✅ Luôn giữ alpha = 1.0f
    colorLayerAdapter.isEnabled = true // ✅ Luôn enable adapter
}
```

### 2. ColorLayerAdapter.kt

#### Xóa check isEnabled trong onClick
**Trước:**
```kotlin
root.onSingleClick {
    if (isEnabled) { // ❌ Block click khi disabled
        onItemClick.invoke(position)
    }
}
```

**Sau:**
```kotlin
root.onSingleClick {
    onItemClick.invoke(position) // ✅ Luôn cho phép click
}
```

## Kết quả

### Hành vi mới:
1. **Khi click btnNone:**
   - Layer được clear (không hiển thị)
   - Color picker vẫn hiển thị và có thể tương tác
   - User có thể scroll và xem các màu
   - User có thể click vào màu để select/highlight

2. **Khi click màu (sau khi chọn None):**
   - ✅ **CHỈ highlight màu được chọn trong color picker**
   - ❌ **KHÔNG apply màu lên character**
   - ❌ **KHÔNG thay đổi layer** (vẫn ở trạng thái None)
   - Character vẫn giữ nguyên trạng thái (layer không hiển thị)

3. **Khi click item có màu (sau khi đã chọn None):**
   - Layer sẽ hiển thị với item và màu mới được chọn
   - Hành vi bình thường như trước

### Ưu điểm:
- ✅ User có thể xem và chọn màu ngay cả khi đang ở trạng thái None
- ✅ UX mượt mà hơn, không bị block tương tác với color picker
- ✅ Không thay đổi character khi click màu ở trạng thái None
- ✅ Giữ được trạng thái màu đã chọn trong color picker để tham khảo

## Các file đã sửa
1. `CustomizeActivity.kt`:
   - `handleNoneLayer()`: Xóa `setColorRecyclerViewEnabled(false)`
   - `handleChangeColorLayer()`: Thêm auto-switch logic, xóa check `if (!isColorEnabled)`
   - `setColorRecyclerViewEnabled()`: Luôn set alpha = 1.0f và isEnabled = true

2. `ColorLayerAdapter.kt`:
   - `onBind()`: Xóa check `if (isEnabled)` trong onClick listener

## Testing
Cần test các trường hợp sau:
1. ✅ Click btnNone → vẫn scroll được color picker
2. ✅ Click btnNone → click màu → CHỈ highlight màu, character KHÔNG thay đổi
3. ✅ Click btnNone → click item có màu → layer hiển thị bình thường
4. ✅ Click item có màu → click màu khác → màu thay đổi bình thường (apply lên character)
5. ✅ Switch giữa các tab (bottom navigation) → trạng thái màu được giữ đúng

## Ngày fix
31/10/2025

