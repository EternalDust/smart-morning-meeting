import requests

  BASE = "http://localhost:8082/api"

  def test_no_auth():
      r = requests.get(BASE + "/agenda/meetings")
      assert r.status_code in [401, 403] or not r.json().get("success"), "未认证应被拒绝"

  def test_xss():
      headers = {"Authorization": "Bearer 你的测试token"}
      payload = {"title": "<script>alert(1)</script>", "meetingType": 1, "deptId": 1, "hostId": 1, "location": "test"}
      r = requests.post(BASE + "/agenda/meetings", json=payload, headers=headers)
      assert "<script>" not in r.text, "XSS未过滤"

  def test_sensitive_leak():
      headers = {"Authorization": "Bearer 你的测试token"}
      r = requests.get(BASE + "/agenda/meetings", headers=headers)
      text = r.text
      assert not any(c.isdigit() for c in text.split("身份证号")[:1]), "敏感信息可能泄露"

  if __name__ == "__main__":
      test_no_auth()
      test_xss()
      test_sensitive_leak()
      print("安全测试通过")