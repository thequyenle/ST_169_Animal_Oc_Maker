# Hướng dẫn xem Log cho Miley Character

## Tổng quan
Đã thêm log chi tiết để debug toàn bộ dữ liệu character Miley ở cả **Suggestion** và **Customize**.

## 1. Log trong SuggestionActivity & SuggestionViewModel

### Tag: `SuggestionViewModel`

#### A. Log Character Data (khi load data)
```
========================================
📊 DEBUG DATA: Miley (Character 1)
========================================
Avatar: [path]
Total layers: [số]

--- Layer 0 ---
  positionCustom: [số]
  positionNavigation: [số]
  imageNavigation: [path]
  Total items: [số]
  ⚠️ LAYER 0 (BODY) DETAILS:
    Item 0:
      image: [path]
      isMoreColors: [true/false]
      colors count: [số]
      color paths:
        [0] [path]
        [1] [path]
        ... and X more colors
```

#### B. Log Randomization Process
```
========================================
🎲 RANDOMIZING MILEY CHARACTER
========================================
Total layers to process: [số]

🔍 Processing layer 0:
  positionCustom: [số]
  positionNavigation: [số]
  items count: [số]
  Random item selected: index=[số]
  Item image: [path]
  Item isMoreColors: [true/false]
  Item colors count: [số]
  ✅ Has colors - selected color index: [số]
  ✅ Color path: [path]
  🔧 LAYER 0 (BODY) - Total colors: [số]
  🔧 BODY LAYER - Using special key=-1 to avoid conflict
  ✅ Saved: key=-1, itemIndex=[số], colorIndex=[số]
  ✅ Path: [path]

========================================
✅ RANDOMIZATION COMPLETE
Total layer selections: [số]
========================================
```

#### C. Log Generated Suggestions
```
========================================
📊 MILEY SUGGESTIONS GENERATED
========================================
Suggestion 0:
  id: [id]
  background: [path]
  randomState layers: [số]
    Layer key=-1: item=[số], color=[số], path=[path]
    Layer key=2: item=[số], color=[số], path=[path]
    ...
========================================
```

#### D. Log Full Render (khi tạo thumbnail)
```
🎨 FULL RENDER: Compositing ALL layers for Miley [id]
🔍 DEBUG: randomState has [số] layers
🔍 Layer key=-1: item=[số], color=[số], path=[path]
🎨 Drawing background: [path]
✅ Background drawn
🎨 Drawing layer key=-1: [path]
✅ Layer key=-1 drawn successfully
✅ FULL RENDER: All layers composited successfully
```

## 2. Log trong CustomizeActivity

### Tag: `CustomizeActivity`

#### A. Log Character Data (khi load vào Customize)
```
========================================
📊 MILEY CHARACTER DATA - CUSTOMIZE - dataObservable
========================================
Avatar: [path]
Total layers: [số]

--- Layer 0 ---
  positionCustom: [số]
  positionNavigation: [số]
  imageNavigation: [path]
  Total items: [số]
  ⚠️ LAYER 0 (BODY) DETAILS:
    Item 0:
      image: [path]
      isMoreColors: [true/false]
      colors count: [số]
      First 3 color paths:
        [0] [path]
        [1] [path]
        ... and X more colors
```

#### B. Log Suggestion Preset (nếu mở từ Suggestion)
```
========================================
📊 MILEY - SUGGESTION PRESET DATA
========================================
isSuggestion: true
categoryPosition: 1
suggestionStateJson: [json]
suggestionBackground: [path]
========================================
```

#### C. Log khi Click Item
```
========================================
🖱️ MILEY - handleFillLayer CLICKED
========================================
Item position: [số]
Item path: [path]
Item colors count: [số]
positionCustom: [số]
positionNavSelected: [số]
✅ pathSelected: [path]
========================================
```

## 3. Log trong CustomizeViewModel

### Tag: `CustomizeViewModel`

#### A. Log Apply Suggestion Preset
```
========================================
📊 APPLYING SUGGESTION PRESET
========================================
Preset has [số] layer selections
Preset layer key=-1: itemIndex=[số], colorIndex=[số], path=[path]
Preset layer key=2: itemIndex=[số], colorIndex=[số], path=[path]

--- Processing storageKey=-1 ---
✅ Converted key=-1 → positionCustom=[số] (body layer)
Found layer: index=[số], positionNav=[số], positionCustom=[số]
Selected item: index=[số], path=[path], isMoreColors=[true/false], colors=[số]
Set pathSelectedList[[số]] = [path]
Set keySelectedItemList[[số]] = [path]
Set color: positionNav=[số], colorIndex=[số]
✅ Updated color list for positionNav=[số], focused color=[số], total colors=[số]
```

#### B. Log setClickFillLayer
```
========================================
🖱️ setClickFillLayer - MILEY
========================================
Item position: [số]
Item path: [path]
Item colors count: [số]
positionNavSelected: [số]
positionCustom: [số]
Has colors - using color[[số]]: [path]
✅ Final pathSelected: [path]
========================================
```

## Cách sử dụng

### 1. Xem log trong Android Studio Logcat:

**Filter cho Suggestion:**
```
SuggestionViewModel
```

**Filter cho Customize:**
```
CustomizeActivity|CustomizeViewModel
```

**Filter cho tất cả Miley logs:**
```
Miley|MILEY
```

### 2. Xem log khi chạy app:

1. Mở **SuggestionActivity** → Xem log character data và randomization
2. Click vào Miley thumbnail → Xem log suggestion được chọn
3. Mở **CustomizeActivity** → Xem log apply preset và load data
4. Click vào item → Xem log handleFillLayer

### 3. Debug Dialog (Long press Miley thumbnail):

Long press vào Miley thumbnail trong SuggestionActivity để xem dialog debug với thông tin tóm tắt.

## Các điểm quan trọng cần chú ý

1. **Body Layer Key**: Miley body layer sử dụng key=-1 để tránh conflict với ears layer
2. **positionCustom vs positionNavigation**: Cần phân biệt rõ 2 giá trị này
3. **Color Path**: Khi có màu, path được lấy từ `listColor[colorIndex].path`, không phải `item.image`
4. **Layer Order**: Các layer được vẽ theo thứ tự key trong randomState

## Troubleshooting

Nếu gặp vấn đề:
1. Check log "RANDOMIZING MILEY CHARACTER" để xem quá trình random
2. Check log "APPLYING SUGGESTION PRESET" để xem quá trình apply
3. Check log "handleFillLayer" để xem path được chọn
4. So sánh path trong log với file thực tế trong assets

