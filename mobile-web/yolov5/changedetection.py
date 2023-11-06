import os
import cv2
import pathlib
import requests
from datetime import datetime

class ChangeDetection:
    result_prev = []
    HOST = 'http://127.0.0.1:8000'
    username = 'admin'
    password = '0000'
    token = '2544a6a9c140b31f9da81ee112b0233ae02993f8'
    title = ''
    text = ''

    def __init__(self, names):
        self.result_prev = [0 for i in range(len(names))]
        res = requests.post(self.HOST + '/api-token-auth/', {
            'username': self.username,
            'password': self.password,
        })
        res.raise_for_status()
        self.token = res.json()['token'] #토큰 저장
        print(self.token)

    def add(self, names, detected_current, save_dir, image,det):
        self.title = ''
        self.text = ''
        change_flag = 0 #변화 감지 플레그
        i=0
        
        while i < len(self.result_prev):
            if self.result_prev[i]==1 and detected_current[i]==1 :
                change_flag = 1
                self.title = names[i]
                # self.text += names[i] + ", "
            i += 1


        self.result_prev = detected_current[:] # 객체 검출 상태 저장
        
        s=""
        for c in det[:,5].unique():
            n = (det[:, 5] == c).sum()  # detections per class
            s += f"{n} {names[int(c)]}{'s' * (n > 1)}, " 
        self.text=s       
        if change_flag==1:
            
            self.send(save_dir, image)

    def send(self, save_dir, image):
        now = datetime.now()
        now.isoformat()

        today = datetime.now()
        save_path = os.getcwd() / save_dir / 'detected' / str(today.year) / str(today.month) / str(today.day)
        pathlib.Path(save_path).mkdir(parents=True, exist_ok=True)
        
        full_path = save_path / '{0}-{1}-{2}-{3}.jpg'.format(today.hour,today.minute,today.second,today.microsecond)
        print(full_path)
        dst = cv2.resize(image, dsize=(320, 240), interpolation=cv2.INTER_AREA)
        cv2.imwrite(full_path, dst)
        
        # 인증이 필요한 요청에 아래의 headers를 붙임
        headers = {'Authorization' : 'JWT ' + self.token, 'Accept' : 'application/json'}
        
        # Post Create
        data = {
        'title' : self.title,
        'text' : self.text,
        'created_date' : now,
        'published_date' : now
        }
        file = {'image' : open(full_path, 'rb')}
        res = requests.post(self.HOST + '/api_root/Post/', data=data, files=file, headers=headers)
        print(res)