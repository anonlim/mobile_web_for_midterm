import requests
HOST = 'http://127.0.0.1:8000'
res = requests.post(HOST + '/api-token-auth/', {
'username’:’username',
'password’:’password',
})
res.raise_for_status()
token = res.json()['token']
print(token)
# 인증이 필요한 요청에 아래의 headers를 붙임
headers = {'Authorization' : 'JWT ' + token, 'Accept' : 'application/json'}
# Post Create
data = {
'title' : '침입자 02',
'text' : 'ㄴ',
'created_date' : '1111-11-11T11:11:00+08:28',
'published_date' : '1111-11-11T11:11:00+08:28'
}
file = {'image' : open('http://127.0.0.1:8000/media/intruder_image/2023/10/09/ss_kjQwSYM.jpg', 'rb')}
res = requests.post(HOST + '/api_root/Post/', data=data, files=file, headers=headers)
print(res)
print(res.json())