<template>
  <div class="bidding">
    <div class="page-header">
      <h1 class="title">投标报价</h1>
      <p class="description">基于成本估算的投标价格生成</p>
    </div>
    
    <div class="info-card">
      <div class="card-title">功能开发中</div>
      <el-empty description="投标报价功能正在开发中，敬请期待..." />
    </div>
  </div>
</template>

<script setup>
// 投标报价页面
</script>

<style lang="scss" scoped>
.bidding {
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
