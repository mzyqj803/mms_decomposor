#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
权限API测试脚本
测试API权限过滤器的功能
"""

import requests
import json
import sys
import os
from typing import Dict, Optional

# 设置控制台编码为UTF-8（Windows）
if sys.platform == 'win32':
    os.system('chcp 65001 >nul')

# 配置
BASE_URL = "http://localhost:8080/api"
TEST_USERNAME = "admin"
TEST_PASSWORD = "admin123"

# 测试用例
TEST_CASES = [
    {
        "name": "测试合同列表API（需要CONTRACT:VIEW权限）",
        "method": "GET",
        "url": f"{BASE_URL}/contracts",
        "required_permission": "CONTRACT:VIEW",
        "expected_status": [200, 403]
    },
    {
        "name": "测试创建合同API（需要CONTRACT:CREATE权限）",
        "method": "POST",
        "url": f"{BASE_URL}/contracts",
        "required_permission": "CONTRACT:CREATE",
        "expected_status": [201, 400, 403],
        "data": {
            "contractNumber": "TEST-001",
            "name": "测试合同"
        }
    },
    {
        "name": "测试装箱单列表API（需要CONTAINER:VIEW权限）",
        "method": "GET",
        "url": f"{BASE_URL}/containers",
        "required_permission": "CONTAINER:VIEW",
        "expected_status": [200, 403]
    },
    {
        "name": "测试零部件列表API（需要COMPONENT:VIEW权限）",
        "method": "GET",
        "url": f"{BASE_URL}/components",
        "required_permission": "COMPONENT:VIEW",
        "expected_status": [200, 403]
    },
    {
        "name": "测试用户列表API（需要USER:VIEW权限）",
        "method": "GET",
        "url": f"{BASE_URL}/users",
        "required_permission": "USER:VIEW",
        "expected_status": [200, 403]
    },
    {
        "name": "测试角色列表API（需要ROLE:VIEW权限）",
        "method": "GET",
        "url": f"{BASE_URL}/roles",
        "required_permission": "ROLE:VIEW",
        "expected_status": [200, 403]
    },
    {
        "name": "测试工艺分解API（需要BREAKDOWN:VIEW权限）",
        "method": "GET",
        "url": f"{BASE_URL}/breakdown/1",
        "required_permission": "BREAKDOWN:VIEW",
        "expected_status": [200, 404, 403]
    }
]


class PermissionAPITester:
    def __init__(self, base_url: str, username: str, password: str):
        self.base_url = base_url
        self.username = username
        self.password = password
        self.session = requests.Session()
        self.token: Optional[str] = None
        
    def login(self) -> bool:
        """登录获取JWT token"""
        try:
            login_url = f"{self.base_url}/auth/login"
            print(f"尝试登录: {login_url}")
            print(f"用户名: {self.username}")
            
            response = self.session.post(
                login_url,
                json={
                    "username": self.username,
                    "password": self.password
                },
                headers={"Content-Type": "application/json"},
                timeout=10
            )
            
            print(f"登录响应状态码: {response.status_code}")
            print(f"登录响应头: {dict(response.headers)}")
            
            if response.status_code == 200:
                data = response.json()
                print(f"登录响应数据: {json.dumps(data, ensure_ascii=False, indent=2)}")
                if "token" in data:
                    self.token = data["token"]
                    self.session.headers.update({
                        "Authorization": f"Bearer {self.token}"
                    })
                    print(f"[OK] 登录成功: {self.username}")
                    return True
                else:
                    print(f"[FAIL] 登录失败: 响应中未找到token")
                    print(f"响应内容: {response.text}")
                    return False
            else:
                print(f"[FAIL] 登录失败: HTTP {response.status_code}")
                try:
                    error_data = response.json()
                    print(f"错误响应: {json.dumps(error_data, ensure_ascii=False, indent=2)}")
                except:
                    print(f"响应内容: {response.text}")
                return False
        except Exception as e:
            print(f"[ERROR] 登录异常: {str(e)}")
            import traceback
            traceback.print_exc()
            return False
    
    def test_api(self, test_case: Dict) -> bool:
        """测试单个API"""
        name = test_case["name"]
        method = test_case["method"]
        url = test_case["url"]
        required_permission = test_case.get("required_permission", "N/A")
        expected_status = test_case.get("expected_status", [200])
        data = test_case.get("data")
        
        print(f"\n{'='*60}")
        print(f"测试: {name}")
        print(f"URL: {url}")
        print(f"方法: {method}")
        print(f"所需权限: {required_permission}")
        print(f"期望状态码: {expected_status}")
        
        try:
            if method == "GET":
                response = self.session.get(url, timeout=10)
            elif method == "POST":
                response = self.session.post(
                    url,
                    json=data,
                    headers={"Content-Type": "application/json"},
                    timeout=10
                )
            elif method == "PUT":
                response = self.session.put(
                    url,
                    json=data,
                    headers={"Content-Type": "application/json"},
                    timeout=10
                )
            elif method == "DELETE":
                response = self.session.delete(url, timeout=10)
            else:
                print(f"[ERROR] 不支持的HTTP方法: {method}")
                return False
            
            status_code = response.status_code
            print(f"实际状态码: {status_code}")
            
            # 检查响应
            if status_code == 403:
                try:
                    error_data = response.json()
                    print(f"权限拒绝响应: {json.dumps(error_data, ensure_ascii=False, indent=2)}")
                    if "required" in error_data:
                        print(f"[OK] 权限检查正常工作，返回了所需权限信息")
                        return True
                except:
                    print(f"权限拒绝响应: {response.text}")
                    return True  # 403表示权限检查工作正常
            
            elif status_code in expected_status:
                print(f"[OK] 请求成功（状态码: {status_code}）")
                if status_code == 200 or status_code == 201:
                    try:
                        result = response.json()
                        print(f"响应数据: {json.dumps(result, ensure_ascii=False, indent=2)[:200]}...")
                    except:
                        print(f"响应内容: {response.text[:200]}...")
                return True
            else:
                print(f"[WARN] 意外的状态码: {status_code} (期望: {expected_status})")
                print(f"响应内容: {response.text[:200]}...")
                return False
                
        except requests.exceptions.RequestException as e:
            print(f"[ERROR] 请求异常: {str(e)}")
            return False
        except Exception as e:
            print(f"[ERROR] 测试异常: {str(e)}")
            return False
    
    def test_unauthorized_access(self):
        """测试未授权访问"""
        print("\n" + "="*60)
        print("测试1: 未授权访问（无Token）")
        print("="*60)
        
        # 清除token
        self.session.headers.pop("Authorization", None)
        
        # 测试一个需要权限的API
        test_case = {
            "name": "未授权访问合同列表API",
            "method": "GET",
            "url": f"{self.base_url}/contracts",
            "required_permission": "CONTRACT:VIEW",
            "expected_status": [401, 403]  # 未授权应该返回401或403
        }
        
        return self.test_api(test_case)
    
    def run_all_tests(self):
        """运行所有测试"""
        print("="*60)
        print("权限API测试开始")
        print("="*60)
        
        # 先测试未授权访问
        self.test_unauthorized_access()
        
        # 再尝试登录
        print("\n" + "="*60)
        print("测试2: 登录并测试权限API")
        print("="*60)
        
        if not self.login():
            print("\n[FAIL] 无法登录，测试终止")
            return False
        
        # 运行测试
        passed = 0
        failed = 0
        
        for test_case in TEST_CASES:
            if self.test_api(test_case):
                passed += 1
            else:
                failed += 1
        
        # 输出总结
        print("\n" + "="*60)
        print("测试总结")
        print("="*60)
        print(f"总测试数: {len(TEST_CASES)}")
        print(f"通过: {passed}")
        print(f"失败: {failed}")
        print("="*60)
        
        return failed == 0


def main():
    """主函数"""
    tester = PermissionAPITester(BASE_URL, TEST_USERNAME, TEST_PASSWORD)
    success = tester.run_all_tests()
    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()

