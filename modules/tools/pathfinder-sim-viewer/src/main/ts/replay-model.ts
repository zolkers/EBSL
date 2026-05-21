export interface ReplayFile {
  readonly results?: readonly ReplayResult[];
}

export interface ReplayResult {
  readonly scenarioId: string;
  readonly description: string;
  readonly status: string;
  readonly reached: boolean;
  readonly ticks: number;
  readonly elapsedNanos: number;
  readonly navigationNodes: number;
  readonly rawNodes: number;
  readonly completePlan: boolean;
  readonly metrics: ReplayMetrics;
  readonly terrain: readonly ReplayBlock[];
  readonly trace: readonly ReplayTick[];
}

export interface ReplayMetrics {
  readonly stuckTicks: number;
  readonly stuckEvents: number;
  readonly longestStuckStreak: number;
  readonly recoveryAttempts?: number;
  readonly bestDistance: number;
  readonly finalDistance: number;
}

export interface ReplayBlock {
  readonly x: number;
  readonly y: number;
  readonly z: number;
  readonly kind: string;
  readonly rgb?: number;
}

export interface ReplayTick {
  readonly tick: number;
  readonly position: Vector3;
  readonly velocity: Vector3;
  readonly status: string;
  readonly moveType: string;
  readonly distanceToGoal: number;
  readonly stuck: boolean;
  readonly jump: boolean;
  readonly sprint: boolean;
  readonly sneak: boolean;
  readonly pathTelemetry?: ReplayPathTelemetry;
}

export type Vector3 = readonly [number, number, number];

export interface ReplayPathTelemetry {
  readonly nearestSegment: number;
  readonly segmentProgress: number;
  readonly lateralError: number;
  readonly verticalError: number;
  readonly speedAlongPath: number;
  readonly speedAcrossPath: number;
}

export function parseReplay(payload: unknown): ReplayResult {
  const candidate = replayCandidate(payload);
  if (!isReplayResult(candidate)) {
    throw new Error("Unsupported pathfinder replay JSON.");
  }
  return candidate;
}

function replayCandidate(payload: unknown): unknown {
  if (isRecord(payload) && Array.isArray(payload.results)) {
    return payload.results[0];
  }
  return payload;
}

function isReplayResult(value: unknown): value is ReplayResult {
  return isRecord(value)
    && typeof value.scenarioId === "string"
    && typeof value.status === "string"
    && Array.isArray(value.terrain)
    && Array.isArray(value.trace);
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}
