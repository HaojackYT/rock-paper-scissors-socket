import numpy as np
import cv2

# 1. Đọc file ảnh (và đồng thời Xử lý: chuyển sang ảnh xám)
# Tham số '0' đảm bảo ảnh được đọc trực tiếp ở chế độ thang độ xám (grayscale)
img = cv2.imread('car3.jpg',0) 

# 3. Hiển thị kết quả
# Mở một cửa sổ mới có tên 'image' và hiển thị ảnh xám đã đọc.
cv2.imshow('image',img) 

# Chờ một sự kiện bàn phím. '0' nghĩa là chờ vô thời hạn.
k = cv2.waitKey(0)

# Phần này là Xử lý/Tương tác người dùng sau khi hiển thị:
if k == 27:
    # Nếu nhấn phím ESC (mã 27), đóng tất cả cửa sổ.
    cv2.destroyAllWindows()
elif k == ord('s'):
    # Nếu nhấn phím 's', lưu ảnh xám hiện tại
    cv2.imwrite('messigray.png',img)
    # và đóng tất cả cửa sổ.
    cv2.destroyAllWindows()