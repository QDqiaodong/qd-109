<template>
  <footer class="footer">
    <div class="container">
      <div class="footer-content">
        <div class="footer-left">
          <span class="logo-icon">🔧</span>
          <span class="logo-text">数码配件经验交流社区</span>
        </div>
        <div class="footer-right">
          <p>仅做经验交流，不涉及商品交易</p>
          <p class="copyright">© 2024 Digital Community. All rights reserved.</p>
        </div>
      </div>
    </div>
  </footer>
</template>

<style lang="scss" scoped>
.footer {
  background: #fff;
  border-top: 1px solid #e8e8e8;
  padding: 24px 0;

  .footer-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .footer-left {
    display: flex;
    align-items: center;
    font-size: 16px;
    font-weight: 600;
    color: #1890ff;

    .logo-icon {
      margin-right: 8px;
      font-size: 20px;
    }
  }

  .footer-right {
    text-align: right;
    color: #999;
    font-size: 12px;

    p {
      margin: 4px 0;
    }
  }
}
</style>
