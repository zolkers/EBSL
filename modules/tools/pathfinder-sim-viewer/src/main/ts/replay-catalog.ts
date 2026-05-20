export interface ReplayCatalogEntry {
  readonly savedAt: string;
  readonly file: string;
  readonly scenarioId: string;
  readonly status: string;
  readonly reached: boolean;
  readonly ticks: number;
  readonly resultCount: number;
}

interface ReplayCatalogFile {
  readonly replays?: readonly ReplayCatalogEntry[];
}

export async function loadReplayCatalog(): Promise<readonly ReplayCatalogEntry[]> {
  const response = await fetch("replays/index.json", { cache: "no-store" });
  if (!response.ok) {
    return [];
  }
  const catalog = await response.json() as ReplayCatalogFile;
  return Array.isArray(catalog.replays) ? catalog.replays : [];
}

export async function loadCatalogReplay(entry: ReplayCatalogEntry): Promise<string> {
  const response = await fetch(`replays/${encodeURIComponent(entry.file)}`, { cache: "no-store" });
  if (!response.ok) {
    throw new Error(`Unable to load saved replay: ${entry.file}`);
  }
  return response.text();
}
