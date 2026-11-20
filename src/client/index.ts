/**
 * Kalimba Minecraft Utility Client
 * Base: InertiaClient (open-source Minecraft hacked client)
 * Base-hunting: RusherHack BaseFinder techniques
 * UI: GrimClient.pl / LiquidBounce design inspiration
 */

import { BaseHunter } from '../modules/baseHunter';
import { UI } from '../render/ui';
import { initEvents } from './events';
import { XRay } from '../modules/xray';
import { Fly } from '../modules/fly';
import { Waypoints } from '../modules/waypoints';
import { Disabler } from '../modules/disabler';

const client = {
  name: 'Kalimba Client',
  version: '1.0.0',
  baseClient: 'InertiaClient',
  modules: {},

  init() {
    console.log(`${this.name} v${this.version} initializing...`);
    console.log(`Base client: ${this.baseClient}`);
    this.ui = new UI();
    this.baseHunter = new BaseHunter();
    this.baseHunter.init();
    initEvents(this.baseHunter);
    
    // Initialize core modules
    this.xray = new XRay();
    this.fly = new Fly();
    this.waypoints = new Waypoints();
    this.disabler = new Disabler();
    
    this.ui.registerModule('Base Hunter', this.baseHunter);
    this.ui.registerModule('X-Ray', this.xray);
    this.ui.registerModule('Fly', this.fly);
    this.ui.registerModule('Waypoints', this.waypoints);
    this.ui.registerModule('Disabler', this.disabler);
    
    this.ui.updateModuleEntries();
    console.log('All modules registered in UI');
  },

  toggleModule(name: string) {
    this.ui.toggleModule(name);
  }
};

export { client };
