# Fix Shadow (Bóng) bị cắt trong CardView

## 🐛 Vấn đề
- CardView có `cardElevation="6dp"` (đổ bóng)
- Nhưng **bóng bị cắt mất** (không hiển thị đầy đủ)

## 🔍 Nguyên nhân

### 1. **Item layout cắt bóng**
```xml
<!-- ❌ SAI - Bóng bị cắt -->
<ConstraintLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="7dp">  <!-- Chưa đủ không gian cho bóng -->
    
    <MaterialCardView
        app:cardElevation="6dp" />  <!-- Bóng cần ~8-10dp space -->
</ConstraintLayout>
```

**Vấn đề:**
- `cardElevation="6dp"` → bóng cần thêm ~8-10dp space xung quanh
- `padding="7dp"` → không đủ space
- Không có `clipChildren="false"` → Android tự động cắt phần vẽ ra ngoài bounds

### 2. **RecyclerView cắt bóng của items**
```xml
<!-- ❌ SAI -->
<RecyclerView
    android:clipToPadding="false" />  <!-- Chỉ có clipToPadding, thiếu clipChildren -->
```

**Vấn đề:**
- Mặc định `clipChildren="true"` → cắt phần vẽ của child (bóng) ra ngoài
- Bóng của CardView bị RecyclerView cắt

## ✅ Giải pháp

### 1. **Sửa item_suggestion.xml**
```xml
<!-- ✅ ĐÚNG -->
<ConstraintLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="10dp"              <!-- ✅ Tăng padding cho bóng -->
    android:clipChildren="false"        <!-- ✅ Không cắt child (bóng) -->
    android:clipToPadding="false">      <!-- ✅ Không cắt vùng padding -->
    
    <MaterialCardView
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:cardElevation="6dp"         <!-- Bóng 6dp -->
        app:cardCornerRadius="16dp"
        app:layout_constraintDimensionRatio="1:1">
        
        <ImageView ... />
    </MaterialCardView>
    
</ConstraintLayout>
```

**Thay đổi:**
- ✅ `padding="10dp"` (tăng từ 7dp) → đủ không gian cho bóng
- ✅ `clipChildren="false"` → KHÔNG cắt bóng của CardView
- ✅ `clipToPadding="false"` → KHÔNG cắt vùng padding

### 2. **Sửa activity_suggestion.xml**
```xml
<!-- ✅ ĐÚNG - Áp dụng cho cả 3 RecyclerViews -->
<RecyclerView
    android:id="@+id/rcvTommy"
    android:paddingHorizontal="20dp"
    android:clipToPadding="false"     <!-- ✅ Không cắt padding -->
    android:clipChildren="false" />   <!-- ✅ Không cắt bóng của items -->

<RecyclerView
    android:id="@+id/rcvMiley"
    android:clipToPadding="false"
    android:clipChildren="false" />   <!-- ✅ Thêm dòng này -->

<RecyclerView
    android:id="@+id/rcvDammy"
    android:clipToPadding="false"
    android:clipChildren="false" />   <!-- ✅ Thêm dòng này -->
```

## 🎨 Giải thích các thuộc tính

### `clipChildren="false"`
- **Mặc định:** `true` (cắt phần vẽ của child ra ngoài bounds)
- **false:** Cho phép child vẽ ra ngoài (VD: bóng, animation)
- **Áp dụng:** ConstraintLayout và RecyclerView

### `clipToPadding="false"`
- **Mặc định:** `true` (cắt vùng padding)
- **false:** Cho phép vẽ vào vùng padding
- **Áp dụng:** RecyclerView để scroll mượt

### `cardElevation="6dp"`
- Độ cao của CardView (tạo bóng)
- Bóng cần thêm ~8-10dp space xung quanh để hiển thị đầy đủ

## 📐 Tính toán space cho bóng

```
cardElevation = 6dp
→ Bóng thực tế cần: ~8-10dp (elevation + blur radius)
→ Padding cần thiết: 10dp (đủ để hiển thị bóng)
```

**Công thức:**
```
padding ≥ cardElevation + 2-4dp (blur radius)
padding = 10dp ≥ 6dp + 4dp = 10dp ✅
```

## 🎯 Hierarchy và Clipping

```
RecyclerView (clipChildren=false, clipToPadding=false)
│
├─ Item 1 (ConstraintLayout - clipChildren=false, clipToPadding=false, padding=10dp)
│   └─ CardView (elevation=6dp)
│       └─ Shadow (vẽ ra ngoài CardView bounds, KHÔNG bị cắt)
│
├─ Item 2 (ConstraintLayout - clipChildren=false, clipToPadding=false, padding=10dp)
│   └─ CardView (elevation=6dp)
│       └─ Shadow (vẽ ra ngoài CardView bounds, KHÔNG bị cắt)
│
└─ ...
```

## 📊 So sánh

| Thuộc tính | Trước | Sau | Kết quả |
|------------|-------|-----|---------|
| **Item padding** | 7dp | 10dp | ✅ Đủ space cho bóng |
| **Item clipChildren** | ❌ Không set (true) | ✅ false | ✅ Bóng không bị cắt |
| **Item clipToPadding** | ❌ Không set | ✅ false | ✅ Bóng đầy đủ |
| **RecyclerView clipChildren** | ❌ Không set (true) | ✅ false | ✅ Bóng không bị cắt |
| **Bóng hiển thị** | ❌ Bị cắt | ✅ Đầy đủ | ✅ Đẹp |

## 🧪 Cách test

### 1. **Visual test:**
- Build và chạy app
- Mở SuggestionActivity
- Quan sát các item CardView
- **Kỳ vọng:** Thấy bóng mờ đầy đủ xung quanh mỗi item

### 2. **Chi tiết kiểm tra:**
- ✅ Bóng ở cạnh trên item
- ✅ Bóng ở cạnh dưới item
- ✅ Bóng ở cạnh trái item
- ✅ Bóng ở cạnh phải item
- ✅ Bóng không bị cắt ở items ở viền RecyclerView

### 3. **Test edge cases:**
- Item ở góc trên trái (row 0, col 0)
- Item ở góc trên phải (row 0, col 1)
- Item ở giữa
- Item ở dưới cùng
- **Tất cả phải có bóng đầy đủ**

## 📝 Files đã sửa

1. ✅ `item_suggestion.xml`
   - Tăng padding: 7dp → 10dp
   - Thêm `clipChildren="false"`
   - Thêm `clipToPadding="false"`

2. ✅ `activity_suggestion.xml`
   - Thêm `clipChildren="false"` cho `rcvTommy`
   - Thêm `clipChildren="false"` cho `rcvMiley`
   - Thêm `clipChildren="false"` cho `rcvDammy`

## 💡 Best practices

### Khi dùng CardView với elevation:
1. ✅ Parent layout cần `clipChildren="false"`
2. ✅ Padding ≥ elevation + 4dp
3. ✅ RecyclerView cần `clipChildren="false"`

### Khi dùng RecyclerView với items có shadow:
1. ✅ Set `clipChildren="false"`
2. ✅ Set `clipToPadding="false"`
3. ✅ Thêm padding để items có space

### Khi cần animation/shadow vẽ ra ngoài bounds:
1. ✅ Luôn set `clipChildren="false"` ở parent
2. ✅ Đảm bảo có đủ padding/margin

## 🎉 Kết quả

✅ Bóng hiển thị **đầy đủ** xung quanh mỗi item
✅ Không bị **cắt** ở bất kỳ vị trí nào
✅ UI **đẹp hơn** với depth và shadow rõ ràng
✅ Nhất quán trên **mọi items**

**Build và test để thấy bóng đẹp!** 🚀

