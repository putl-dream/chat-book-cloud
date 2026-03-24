/// <reference types="cypress" />

describe('Article Sidebar Viewport Regression', () => {
  const testViewports = [
    { width: 320, height: 568, name: 'Mobile Portrait (iPhone SE)' },
    { width: 768, height: 1024, name: 'Tablet Portrait (iPad)' },
    { width: 1024, height: 768, name: 'Tablet Landscape' },
    { width: 1440, height: 900, name: 'Laptop' },
    { width: 1920, height: 1080, name: 'Desktop' },
  ];

  beforeEach(() => {
    // 假设路由挂载在 /article/1
    cy.visit('/article/1');
  });

  testViewports.forEach((vp) => {
    it(`should not have horizontal scrollbar on ${vp.name} (${vp.width}x${vp.height})`, () => {
      cy.viewport(vp.width, vp.height);
      
      // 等待可能存在的网络请求及渲染完成
      cy.get('.article-page').should('be.visible');

      // 验证没有横向滚动条
      // 通过获取 window 的 scrollX 属性和 document.documentElement.scrollWidth 等判断
      cy.window().then((win) => {
        const documentWidth = win.document.documentElement.scrollWidth;
        const windowWidth = win.innerWidth;
        
        // 期望文档整体宽度不大于视口宽度
        expect(documentWidth).to.be.at.most(windowWidth);
      });
      
      // 验证侧边栏是否根据视口折叠/展开
      if (vp.width <= 1024) {
        cy.get('.sidebar').should('have.class', 'is-collapsed');
        // 测试 hover / click 展开行为
        cy.get('.sidebar').trigger('mouseenter');
        cy.get('.sidebar').should('have.class', 'is-expanded');
        cy.get('.sidebar').trigger('mouseleave');
        cy.get('.sidebar').should('not.have.class', 'is-expanded');
      } else {
        cy.get('.sidebar').should('not.have.class', 'is-collapsed');
      }
    });
  });
});
