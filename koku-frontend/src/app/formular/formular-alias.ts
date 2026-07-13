export function formularAliasTestId(alias: string | undefined): string | null {
  const normalizedAlias = alias?.trim().replace(/\s+/g, '-');
  return normalizedAlias ? `${normalizedAlias}-form` : null;
}
