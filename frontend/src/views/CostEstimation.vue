<template>
  <div class="cost-estimation">
    <div class="page-header">
      <h1 class="title">成本估算</h1>
      <p class="description">零部件成本和工艺成本分析</p>
    </div>
    
    <div class="info-card">
      <div class="card-title">功能开发中</div>
      <el-empty description="成本估算功能正在开发中，敬请期待..." />
    </div>
  </div>
</template>

<script setup>
// 成本估算页面
</script>

<style lang="scss" scoped>
.cost-estimation {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  
  .title {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
  }
  
  .description {
    color: #606266;
    font-size: 14px;
  }
}

.info-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 40px;
  text-align: center;
  
  .card-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 20px;
  }
}
</style>
