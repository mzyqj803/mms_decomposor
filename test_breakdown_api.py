import requests
import time

# API endpoint
url = "http://localhost:8080/api/breakdown/contract/3"

print("开始测试工艺分解API...")
print("=" * 60)

# 记录开始时间
start_time = time.time()

try:
    # 调用API
    response = requests.post(url, headers={"Content-Type": "application/json"}, timeout=300)
    
    # 记录结束时间
    end_time = time.time()
    duration = end_time - start_time
    
    # 打印结果
    print(f"✅ API调用成功！")
    print(f"HTTP状态码: {response.status_code}")
    print(f"总耗时: {duration:.2f} 秒")
    print("=" * 60)
    
    # 解析响应
    if response.status_code == 200:
        data = response.json()
        print(f"合同ID: {data.get('contractId')}")
        print(f"箱包总数: {data.get('totalContainers')}")
        print(f"处理部件总数: {data.get('totalProcessedComponents')}")
        print(f"问题部件数: {len(data.get('allProblemComponents', []))}")
        
except requests.exceptions.Timeout:
    end_time = time.time()
    duration = end_time - start_time
    print(f"❌ API调用超时")
    print(f"超时时间: {duration:.2f} 秒")
    
except Exception as e:
    end_time = time.time()
    duration = end_time - start_time
    print(f"❌ API调用失败: {str(e)}")
    print(f"耗时: {duration:.2f} 秒")

