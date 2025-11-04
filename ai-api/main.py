from fastapi import FastAPI, UploadFile, File
from fastapi.middleware.cors import CORSMiddleware
import cv2
import mediapipe as mp
import numpy as np
import asyncio
from concurrent.futures import ThreadPoolExecutor
import uvicorn
import os

# Ẩn warning TensorFlow/MediaPipe
os.environ["TF_CPP_MIN_LOG_LEVEL"] = "3"

# ========== CẤU HÌNH ==========
app = FastAPI(title="✋ API Nhận diện Kéo Búa Bao")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Cho phép mọi nguồn truy cập
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Khởi tạo MediaPipe 1 lần (không tạo lại mỗi lần request)
mp_hands = mp.solutions.hands
mp_drawing = mp.solutions.drawing_utils

# Thread pool cho xử lý song song (đa luồng)
executor = ThreadPoolExecutor(max_workers=4)  # Có thể chỉnh tùy CPU


# ========== HÀM XỬ LÝ ==========
def get_finger_status(hand_landmarks):
    fingers = []
    lm = hand_landmarks.landmark

    # Ngón cái (theo trục x)
    if lm[4].x < lm[3].x:
        fingers.append(1)
    else:
        fingers.append(0)

    # Các ngón còn lại (theo trục y)
    for tip, pip in zip([8, 12, 16, 20], [6, 10, 14, 18]):
        fingers.append(1 if lm[tip].y < lm[pip].y else 0)

    return fingers


def classify_gesture(fingers):
    total = sum(fingers)
    if total == 0:
        return "BÚA"
    elif total == 2 and fingers[1] == 1 and fingers[2] == 1:
        return "KÉO"
    elif total == 5:
        return "BAO"
    else:
        return "KHÔNG RÕ"


def process_image(image: np.ndarray) -> str:
    """Xử lý ảnh trong luồng riêng"""
    rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)

    # Mỗi luồng tạo 1 instance riêng (thread-safe)
    with mp_hands.Hands(static_image_mode=True, max_num_hands=1,
                        min_detection_confidence=0.7) as hands:
        result = hands.process(rgb)

    gesture = "Không thấy tay"
    if result.multi_hand_landmarks:
        for hand_landmarks in result.multi_hand_landmarks:
            fingers = get_finger_status(hand_landmarks)
            gesture = classify_gesture(fingers)

    return gesture


# ========== API ENDPOINT ==========
@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    """Xử lý ảnh nhận diện tay - chạy bất đồng bộ + đa luồng"""
    contents = await file.read()
    np_arr = np.frombuffer(contents, np.uint8)
    image = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)

    if image is None:
        return {"error": "Không thể đọc ảnh"}

    loop = asyncio.get_event_loop()
    gesture = await loop.run_in_executor(executor, process_image, image)

    return {"gesture": gesture}


# ========== CHẠY SERVER ==========
if __name__ == "__main__":
    print("🚀 Server đang chạy tại: http://127.0.0.1:8000")
    # Chạy đa tiến trình (4 worker) — mỗi worker có ThreadPoolExecutor riêng
    uvicorn.run("main:app", host="0.0.0.0", port=8000, workers=4)