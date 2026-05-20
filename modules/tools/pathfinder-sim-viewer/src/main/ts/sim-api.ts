export interface SimGoalParameter {
  readonly id: string;
  readonly label: string;
  readonly defaultValue: number;
}

export interface SimGoalDescriptor {
  readonly id: string;
  readonly label: string;
  readonly description: string;
  readonly mode: string;
  readonly parameters: readonly SimGoalParameter[];
}

export interface MinecraftRouteRequest {
  readonly worldDirectory: string;
  readonly startX: number;
  readonly startY: number;
  readonly startZ: number;
  readonly goalId: string;
  readonly goalValues: Readonly<Record<string, number>>;
  readonly maxTicks: number;
  readonly radiusChunks: number;
  readonly goalSearchBlocks: number;
  readonly saveReplay: boolean;
}

export async function loadSimGoals(): Promise<readonly SimGoalDescriptor[]> {
  const response = await fetch("api/goals", { cache: "no-store" });
  if (!response.ok) {
    return [];
  }
  const goals = await response.json() as readonly SimGoalDescriptor[];
  return Array.isArray(goals) ? goals : [];
}

export async function runMinecraftRoute(request: MinecraftRouteRequest): Promise<string> {
  const response = await fetch("api/simulations/minecraft", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request)
  });
  if (!response.ok) {
    throw new Error(`Simulation failed: HTTP ${response.status}`);
  }
  return response.text();
}
