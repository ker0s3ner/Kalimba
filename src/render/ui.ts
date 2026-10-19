/**
 * UI Module - Inspired by GrimClient.pl and LiquidBounce
 * Provides a modern, intuitive module-based UI similar to LiquidBounce's interface
 */

export class UI {
  constructor() {
    this.modules = new Map();
    this.activeModule = null;
    this.init();
  }

  init() {
    this.createUIFramework();
    this.setupModuleToggles();
    this.setupRenderLoop();
  }

  createUIFramework() {
    // Create the main UI container
    const uiContainer = document.createElement('div');
    uiContainer.id = 'freebuff-ui';
    uiContainer.style.cssText = `
      position: fixed;
      bottom: 20px;
      left: 20px;
      width: 320px;
      background: rgba(15, 15, 15, 0.8);
      border-radius: 8px;
      padding: 15px;
      color: #fff;
      font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
      z-index: 1000;
      backdrop-filter: blur(10px);
    `;

    // Title bar
    const title = document.createElement('div');
    title.style.cssText = `
      font-size: 14px;
      font-weight: bold;
      margin-bottom: 12px;
      padding-bottom: 8px;
      border-bottom: 1px solid #333;
    `;
    title.textContent = 'Freebuff Client';

    // Modules container
    const modulesDiv = document.createElement('div');
    modulesDiv.style.cssText = `
      max-height: 200px;
      overflow-y: auto;
      margin-top: 10px;
    `;

    // Add module entries (will be populated dynamically)
    modulesDiv.innerHTML = this.getModuleEntries();

    uiContainer.appendChild(title);
    uiContainer.appendChild(modulesDiv);
    document.body.appendChild(uiContainer);
  }

  getModuleEntries(): string {
    // Return HTML for module toggles - will be populated by modules
    return `
      <div style="margin-top: 10px;">
        <span style="font-size: 12px; color: #888;">
          No modules loaded yet
        </span>
      </div>
    `;
  }

  setupModuleToggles() {
    // Handle module toggle clicks
    document.addEventListener('click', (e) => {
      const target = e.target.closest('.module-toggle');
      if (target) {
        const moduleName = target.dataset.module;
        this.toggleModule(moduleName);
      }
    });
  }

  toggleModule(moduleName: string) {
    const module = this.modules.get(moduleName);
    if (module) {
      module.toggle();
      this.updateModuleUI(module);
    }
  }

  updateModuleUI(module: any) {
    const toggle = document.querySelector(`[data-module="${module.name}"]`);
    if (toggle) {
      toggle.classList.toggle('active', module.isEnabled());
      toggle.textContent = module.isEnabled() ? 'ON' : 'OFF';
    }
  }

  registerModule(name: string, module: any) {
    this.modules.set(name, module);
    // Add UI entry for this module
    this.updateModuleEntries();
  }

  updateModuleEntries() {
    const entries = Array.from(this.modules.entries())
      .map(([name, module]) => `
        <div style="margin: 5px 0;">
          <span class="module-toggle" data-module="${name}" 
                style="cursor: pointer; user-select: none;">
            ${name}: ${module.isEnabled() ? 'ON' : 'OFF'}
          </span>
        </div>
      `)
      .join('');

    // Update the UI
    const container = document.querySelector('#freebuff-ui .modules');
    if (container) {
      container.innerHTML = entries;
    }
  }

  setupRenderLoop() {
    // Basic render loop for UI updates
    let lastTime = 0;
    const render = (time: number) => {
      const delta = time - lastTime;
      lastTime = time;
      this.update(delta);
      requestAnimationFrame(render);
    };
    requestAnimationFrame(render);
  }

  update(delta: number) {
    // Update UI state
  }
}