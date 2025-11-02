# Hướng dẫn Debug: Tại sao ảnh không hiển thị?

## 📋 Mô tả vấn đề
Ảnh có trong assets nhưng không hiển thị trên ImageView ở vị trí mong muốn.

## 🔍 Log Debug đã thêm

### 1. Log cho Body Layer (index = 0)
```
CustomizeActivity  D  Layer[0]: posNav=0, posCus=0, path=body.png
CustomizeActivity  D    → RENDER to BODY ImageView
CustomizeActivity  D       ├─ Path: character_1/body/body.png
CustomizeActivity  D       ├─ BodyImageView: androidx.appcompat.widget.AppCompatImageView{...}
CustomizeActivity  D       ├─ BodyImageView ID: 2131296784
CustomizeActivity  D       ├─ BodyImageView Visibility: 0
CustomizeActivity  D       ├─ BodyImageView Alpha: 1.0
CustomizeActivity  D       ├─ BodyImageView Size: 1080x1920
CustomizeActivity  D       ├─ File exists in assets: true
CustomizeActivity  D       └─ ✓ BODY GLIDE SUCCESS: 1024x1024
```

### 2. Log cho các Layer khác (index > 0)
```
CustomizeActivity  D  Layer[5]: posNav=5, posCus=9, path=8.png
CustomizeActivity  D    → RENDER to ImageView[9]
CustomizeActivity  D       ├─ Path: character_1/tail/8.png
CustomizeActivity  D       ├─ ImageView: androidx.appcompat.widget.AppCompatImageView{...}
CustomizeActivity  D       ├─ ImageView ID: 2131296785
CustomizeActivity  D       ├─ ImageView Visibility: 0
CustomizeActivity  D       ├─ ImageView Alpha: 1.0
CustomizeActivity  D       ├─ ImageView Size: 1080x1920
CustomizeActivity  D       ├─ ImageView Parent: androidx.constraintlayout.widget.ConstraintLayout{...}
CustomizeActivity  D       ├─ File exists in assets: true
CustomizeActivity  D       └─ ✓ GLIDE SUCCESS: 1024x1024
```

## ❌ Các trường hợp lỗi thường gặp

### Lỗi 1: ImageView bị NULL
```
CustomizeActivity  E       └─ ✗ ImageView is NULL at position 9
```
**Nguyên nhân:** 
- `imageViewList` không có đủ phần tử
- Index sai
- ImageView chưa được khởi tạo

**Giải pháp:**
- Kiểm tra `viewModel.imageViewList.value.size`
- Kiểm tra khởi tạo ImageView trong `onCreate()`

---

### Lỗi 2: File không tồn tại trong assets
```
CustomizeActivity  E       └─ ✗ File NOT found in assets: java.io.FileNotFoundException
CustomizeActivity  D       ├─ File exists in assets: false
```
**Nguyên nhân:**
- Đường dẫn file sai
- File không có trong folder assets
- Sai tên file (phân biệt hoa thường)

**Giải pháp:**
- Kiểm tra path trong log: `├─ Path: ...`
- Mở folder assets và kiểm tra file có tồn tại không
- Rebuild project: Build > Clean Project > Rebuild Project

---

### Lỗi 3: Glide load thất bại
```
CustomizeActivity  E       └─ ✗ GLIDE LOAD FAILED: Failed to load resource
```
**Nguyên nhân:**
- File bị corrupt
- Format ảnh không hỗ trợ
- Memory không đủ
- Path không đúng định dạng

**Giải pháp:**
- Kiểm tra file ảnh có mở được bằng image viewer không
- Thử load ảnh khác để test
- Check logcat để xem chi tiết lỗi từ `logRootCauses()`

---

### Lỗi 4: ImageView không visible
```
CustomizeActivity  D       ├─ ImageView Visibility: 8  (GONE)
CustomizeActivity  D       ├─ ImageView Alpha: 0.0
```
**Nguyên nhân:**
- ImageView bị ẩn (GONE hoặc INVISIBLE)
- Alpha = 0 (trong suốt hoàn toàn)
- Parent bị ẩn

**Giải pháp:**
```kotlin
imageView.visibility = View.VISIBLE  // 0 = VISIBLE, 4 = INVISIBLE, 8 = GONE
imageView.alpha = 1.0f
```

---

### Lỗi 5: ImageView Size = 0x0
```
CustomizeActivity  D       ├─ ImageView Size: 0x0
```
**Nguyên nhân:**
- Layout chưa được measure
- Constraint layout thiếu constraints
- Width/Height = 0dp mà không có constraint

**Giải pháp:**
- Kiểm tra XML layout của ImageView
- Đảm bảo có width/height hoặc constraints đầy đủ
- Chờ layout được measure xong mới load ảnh

---

### Lỗi 6: Ảnh load thành công nhưng bị ghi đè
```
CustomizeActivity  D  Layer[21]: posNav=21, posCus=21, path=tail_B.png
CustomizeActivity  D       └─ ✓ GLIDE SUCCESS: 1024x1024
CustomizeActivity  D  Layer[18]: posNav=18, posCus=21, path=tail_A.png  ← SAI!
CustomizeActivity  D       └─ ✓ GLIDE SUCCESS: 1024x1024
```
**Nguyên nhân:**
- Nhiều layer cùng render vào 1 ImageView (positionCustom trùng nhau)
- Logic mapping positionCustom bị sai

**Giải pháp:**
- Kiểm tra `layerListModel.positionCustom` có unique không
- Fix logic mapping trong ViewModel
- Đảm bảo mỗi layer có 1 ImageView riêng

---

## 🛠️ Cách sử dụng Log Debug

### Bước 1: Chạy app và thao tác
1. Mở app
2. Chọn Random All → chọn đuôi khác
3. Quan sát hành vi (ảnh có hiển thị không?)

### Bước 2: Lọc log trong Logcat
```
Tag: CustomizeActivity
Level: Debug
Hoặc search: "RENDER to ImageView"
```

### Bước 3: Phân tích log
Tìm layer có vấn đề, ví dụ:
```
Layer[5]: posNav=5, posCus=9, path=8.png
  → RENDER to ImageView[9]
```

Kiểm tra các thông tin:
1. ✅ **ImageView**: Có null không?
2. ✅ **Visibility**: = 0 (VISIBLE)?
3. ✅ **Alpha**: = 1.0?
4. ✅ **Size**: > 0x0?
5. ✅ **File exists**: = true?
6. ✅ **GLIDE**: SUCCESS hay FAILED?

### Bước 4: Xác định nguyên nhân
- Nếu bất kỳ check nào FAILED → đó là nguyên nhân
- Nếu tất cả ✅ nhưng ảnh vẫn không hiện → check xem có layer khác ghi đè không (cùng `posCus`)

---

## 📊 Ví dụ thực tế

### Trường hợp: Đuôi A không mất khi chọn đuôi B

**Log khi chọn đuôi B:**
```
Layer[21]: posNav=21, posCus=21, path=character_1/tail/tail_B.png
  → RENDER to ImageView[21]
     ├─ Path: character_1/tail/tail_B.png
     ├─ ImageView: AppCompatImageView{...}
     ├─ File exists in assets: true
     └─ ✓ GLIDE SUCCESS: 1024x1024

Layer[18]: posNav=18, posCus=21, path=character_1/tail/tail_A.png  ← DUPLICATE!
  → RENDER to ImageView[21]
     ├─ Path: character_1/tail/tail_A.png
     └─ ✓ GLIDE SUCCESS: 1024x1024
```

**Vấn đề:** 
- Layer 21 (đuôi B) render vào ImageView[21] ✅
- Layer 18 (đuôi A) CŨNG render vào ImageView[21] ❌ → Ghi đè đuôi B!

**Nguyên nhân:**
- `positionCustom` của layer 18 và 21 đều = 21 (trùng nhau)

**Giải pháp:**
- Fix mapping `positionCustom` trong data
- Hoặc khi chọn đuôi B, phải clear path của đuôi A trước

---

## 🎯 Checklist Debug

- [ ] Log có hiển thị "→ RENDER to ImageView[X]"?
- [ ] ImageView có NULL không?
- [ ] ImageView Visibility = 0 (VISIBLE)?
- [ ] ImageView Alpha = 1.0?
- [ ] ImageView Size > 0x0?
- [ ] File exists in assets = true?
- [ ] Glide load SUCCESS?
- [ ] Có layer khác cùng render vào ImageView này không? (check `posCus` trùng)
- [ ] ImageView có bị che bởi layer khác không? (z-index/elevation)

---

## 📝 Ghi chú

- Log này chỉ chạy ở Debug build
- Nếu cần disable log: Comment các dòng `Log.d()` trong `renderAllLayers()`
- Log rất chi tiết nên có thể làm chậm app, chỉ dùng khi debug

---

**Cập nhật:** 31/10/2025

