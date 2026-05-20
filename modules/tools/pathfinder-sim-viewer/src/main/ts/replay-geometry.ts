import type { ReplayBlock, ReplayResult, Vector3 } from "./replay-model.js";
import type { ViewerState } from "./viewer-state.js";
import { clamp } from "./math-utils.js";

export interface Bounds {
  readonly minX: number;
  readonly maxX: number;
  readonly minZ: number;
  readonly maxZ: number;
}

export interface Point2 {
  readonly x: number;
  readonly y: number;
}

export function computeBounds(result: ReplayResult): Bounds {
  const xs: number[] = [];
  const zs: number[] = [];
  for (const block of result.terrain) {
    xs.push(block.x, block.x + 1);
    zs.push(block.z, block.z + 1);
  }
  for (const tick of result.trace) {
    xs.push(tick.position[0]);
    zs.push(tick.position[2]);
  }
  return {
    minX: Math.min(...xs),
    maxX: Math.max(...xs),
    minZ: Math.min(...zs),
    maxZ: Math.max(...zs)
  };
}

export function surfaceTerrain(terrain: readonly ReplayBlock[]): ReplayBlock[] {
  const columns = new Map<string, ReplayBlock>();
  for (const block of terrain) {
    const key = `${block.x},${block.z}`;
    const current = columns.get(key);
    if (current === undefined || block.y > current.y) {
      columns.set(key, block);
    }
  }
  return [...columns.values()];
}

export function minTerrainY(terrain: readonly ReplayBlock[]): number {
  if (terrain.length === 0) {
    return 0;
  }
  return terrain.reduce((min, block) => Math.min(min, block.y), terrain[0]?.y ?? 0);
}

export function screenPoint(bounds: Bounds, state: ViewerState, rect: DOMRect, position: Vector3): Point2 {
  return {
    x: screenX(bounds, state, rect, position[0]),
    y: screenY(bounds, state, rect, position[2])
  };
}

export function screenX(bounds: Bounds, state: ViewerState, rect: DOMRect, x: number): number {
  const span = Math.max(1, bounds.maxX - bounds.minX);
  return transformX(state, 48 + (x - bounds.minX) / span * (rect.width - 96));
}

export function screenY(bounds: Bounds, state: ViewerState, rect: DOMRect, z: number): number {
  const span = Math.max(1, bounds.maxZ - bounds.minZ);
  return transformY(state, rect.height - 48 - (z - bounds.minZ) / span * (rect.height - 96));
}

export function isoPoint(
  bounds: Bounds,
  state: ViewerState,
  rect: DOMRect,
  terrainMinY: number,
  x: number,
  y: number,
  z: number
): Point2 {
  const centerX = (bounds.minX + bounds.maxX) * 0.5;
  const centerZ = (bounds.minZ + bounds.maxZ) * 0.5;
  const scale = isoScale(bounds, rect);
  const rotated = rotate(state, x - centerX, z - centerZ);
  return {
    x: transformX(state, (rotated.x - rotated.z) * scale + rect.width * 0.5),
    y: transformY(state, (rotated.x + rotated.z) * scale * 0.5 + rect.height * 0.58 - (y - terrainMinY) * scale * 0.8)
  };
}

export function isoDepth(bounds: Bounds, state: ViewerState, block: ReplayBlock): number {
  const centerX = (bounds.minX + bounds.maxX) * 0.5;
  const centerZ = (bounds.minZ + bounds.maxZ) * 0.5;
  const rotated = rotate(state, block.x - centerX, block.z - centerZ);
  return rotated.x + rotated.z + block.y * 0.35;
}

function isoScale(bounds: Bounds, rect: DOMRect): number {
  const span = Math.max(1, bounds.maxX - bounds.minX, bounds.maxZ - bounds.minZ);
  return clamp(Math.min(rect.width, rect.height) / (span * 1.55), 5, 22);
}

function rotate(state: ViewerState, x: number, z: number): { readonly x: number; readonly z: number } {
  const cos = Math.cos(state.yaw);
  const sin = Math.sin(state.yaw);
  return {
    x: x * cos - z * sin,
    z: x * sin + z * cos
  };
}

function transformX(state: ViewerState, x: number): number {
  return x * state.zoom + state.panX;
}

function transformY(state: ViewerState, y: number): number {
  return y * state.zoom + state.panY;
}
