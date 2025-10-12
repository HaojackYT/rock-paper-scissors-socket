from fastapi import FastAPI, UploadFile, File
from fastapi.middleware.cors import CORSMiddleware
import cv2
import mediapipe as mp
import numpy as np
import uvicorn
import os
os.environ["TF_CPP_MIN_LOG_LEVEL"] = "3"  # Ẩn warning TensorFlow/MediaPipe

# ========== CẤU HÌNH ==========

app = FastAPI(title="✋ API Nhận diện Kéo Búa Bao")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Cho phép mọi nguồn truy cập (hoặc thay bằng domain cụ thể)
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

mp_hands = mp.solutions.hands
mp_drawing = mp.solutions.drawing_utils


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
        if lm[tip].y < lm[pip].y:
            fingers.append(1)
        else:
            fingers.append(0)
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


# ========== API ENDPOINT ==========

@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    # Đọc file ảnh từ người dùng gửi lên
    contents = await file.read()
    np_arr = np.frombuffer(contents, np.uint8)
    image = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)

    if image is None:
        return {"error": "Không thể đọc ảnh"}

    rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)

    with mp_hands.Hands(static_image_mode=True, max_num_hands=1, min_detection_confidence=0.7) as hands:
        result = hands.process(rgb)

    gesture = "Không thấy tay"

    if result.multi_hand_landmarks:
        for hand_landmarks in result.multi_hand_landmarks:
            fingers = get_finger_status(hand_landmarks)
            gesture = classify_gesture(fingers)

    return {"gesture": gesture}


# ========== CHẠY SERVER ==========

if __name__ == "__main__":
    print("🚀 Server đang chạy tại: http://127.0.0.1:8000")
    uvicorn.run(app, host="0.0.0.0", port=8000)
