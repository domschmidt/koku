export interface RouteBasedContentOverride {
  '@type': string;
  alias?: string;
  disabled?: boolean;
  routeParam?: string;
}

export interface ResolvedContentOverride {
  alias: string;
  disabled: boolean;
  value: string;
}

export const replaceRouteSegments = (
  rawValue: string | undefined,
  routeSegments: Record<string, string> | null | undefined,
) => {
  let result = rawValue || '';
  for (const [segment, value] of Object.entries(routeSegments || {})) {
    result = result.replace(segment, value);
  }
  return result;
};

export const resolveRoutePath = (
  route: string | undefined,
  routeSegments: Record<string, string> | null | undefined,
) => {
  if (!route) {
    return undefined;
  }
  return route
    .split('/')
    .map((part) => (part.startsWith(':') ? routeSegments?.[part] : part))
    .join('/');
};

export const resolveRouteBasedContentOverrides = <TOverride extends RouteBasedContentOverride>(
  overrides: readonly TOverride[] | undefined,
  routeSegments: Record<string, string> | null | undefined,
): ResolvedContentOverride[] => {
  const result: ResolvedContentOverride[] = [];
  for (const override of overrides || []) {
    if (override['@type'] !== 'route-based-override' || !override.alias) {
      continue;
    }
    const value = routeSegments?.[override.routeParam || ''];
    if (value !== undefined) {
      result.push({
        alias: override.alias,
        disabled: override.disabled === true,
        value,
      });
    }
  }
  return result;
};
