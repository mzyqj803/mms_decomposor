import urllib.request
import json
import time

# API endpoint
url = "http://localhost:8080/api/breakdown/contract/3"

print("开始测试工艺分解API...")
print("=" * 60)

# 记录开始时间
start_time = time.time()

try:
    # 创建请求
    req = urllib.request.Request(url, method='POST')
    req.add_header('Content-Type', 'application/json')
    
    # 调用API
    with urllib.request.urlopen(req, timeout=300) as response:
        # 记录结束时间
        end_time = time.time()
        duration = end_time - start_time
        
        # 读取响应
        data = json.loads(response.read().decode('utf-8'))
        
        # 打印结果
        print("API成功!")
        print(f"HTTP状态码: {response.status}")
        print(f"总耗时: {duration:.2f} 秒")
        print("=" * 60)
        print(f"合同ID: {data.get('contractId')}")
        print(f"箱包总数: {data.get('totalContainers')}")
        print(f"处理部件总数: {data.get('totalProcessedComponents')}")
        print(f"问题部件数: {len(data.get('allProblemComponents', []))}")
        
except urllib.error.URLError as e:
    end_time = time.time()
    duration = end_time - start_time
    print(f"API失败: {str(e)}")
    print(f"耗时: {duration:.2f} 秒")
    
except Exception as e:
    end_time = time.time()
    duration = end_time - start_time
    print(f"错误: {str(e)}")
    print(f"耗时: {duration:.2f} 秒")

