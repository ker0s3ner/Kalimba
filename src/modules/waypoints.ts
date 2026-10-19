/**
 * Waypoints Module - Save and view locations
 * Inspired by waypoint systems in various Minecraft clients
 */

export class Waypoints {
  constructor() {
    this.waypoints = [];
  }

  add(name: string, x: number, y: number, z: number) {
    this.waypoints.push({ name, x, y, z });
    console.log(`Waypoint added: ${name} at ${x}, ${y}, ${z}`);
  }

  getAll() {
    return this.waypoints;
  }

  renderUI(ui: any) {
    // Render waypoints in the client UI
    console.log('Rendering waypoints in UI...');
    this.waypoints.forEach(wp => {
      console.log(`Waypoint: ${wp.name} at (${wp.x}, ${wp.y}, ${wp.z})`);
    });
  }
}