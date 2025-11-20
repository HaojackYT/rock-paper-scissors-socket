# ================================
# 1️⃣ Base image
# ================================
FROM python:3.10-slim

# ================================
# 2️⃣ Tối ưu môi trường
# ================================
ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    TZ=Asia/Ho_Chi_Minh \
    LANG=C.UTF-8

# ================================
# 3️⃣ Cài các gói hệ thống
# ================================
RUN apt-get update && apt-get install -y \
    libgl1 \
    libglib2.0-0 \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# ================================
# 4️⃣ Tạo thư mục dự án
# ================================
WORKDIR /app

# ================================
# 5️⃣ Cài dependencies
# ================================
COPY requirements.txt .
RUN pip install --upgrade pip \
    && pip install --no-cache-dir -r requirements.txt

# ================================
# 6️⃣ Copy toàn bộ source code
# ================================
COPY . .

# ================================
# 7️⃣ Expose port FastAPI
# ================================
EXPOSE 8000

# ================================
# 8️⃣ Chạy ứng dụng
#    ⚠ Nếu file chính của bạn KHÔNG phải main.py thì sửa bên dưới
# ================================
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000", "--workers", "4"]
