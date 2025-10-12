# ================================
# 1️⃣ Base image
# ================================
FROM python:3.10-slim

# ================================
# 2️⃣ Cài các gói hệ thống cần thiết
# ================================
RUN apt-get update && apt-get install -y \
    libgl1 \
    libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

# ================================
# 3️⃣ Tạo thư mục làm việc
# ================================
WORKDIR /app

# ================================
# 4️⃣ Copy file cần thiết
# ================================
COPY requirements.txt requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# ================================
# 5️⃣ Expose port FastAPI
# ================================
EXPOSE 8000

# ================================
# 6️⃣ Chạy ứng dụng bằng Uvicorn
# ================================
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
