<template>
  <div class="production-plan">
    <div class="page-header">
      <h1 class="title">生产计划</h1>
      <p class="description">基于工艺工序的最优制造流程和生产计划</p>
    </div>
    
    <div class="info-card">
      <div class="card-title">功能开发中</div>
      <el-empty description="生产计划功能正在开发中，敬请期待..." />
    </div>
  </div>
</template>

<script setup>
// 生产计划页面
</script>

<style lang="scss" scoped>
.production-plan {
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
