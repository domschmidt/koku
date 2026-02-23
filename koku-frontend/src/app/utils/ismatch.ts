const deepMatch = (object: any, source: any): boolean => {
  // Wenn source kein Objekt ist, einfach vergleichen
  if (typeof source !== 'object' || source === null) {
    return object === source;
  }

  // Wenn beide Arrays sind, prüfe ob jedes Element von source auch in object vorkommt
  if (Array.isArray(source)) {
    if (!Array.isArray(object)) return false;
    return source.every((v) => object.includes(v));
  }

  // Für Objekte: alle Keys prüfen
  return Object.keys(source).every((key) => {
    if (!(key in object)) return false; // Key muss existieren

    const srcVal = source[key];
    const objVal = object[key];

    if (Array.isArray(srcVal) && Array.isArray(objVal)) {
      return srcVal.every((v) => objVal.includes(v));
    }

    if (typeof srcVal === 'object' && srcVal !== null) {
      return deepMatch(objVal, srcVal);
    }

    return objVal === srcVal;
  });
};

export const isMatch = (object: any, source: any): boolean => {
  return object === source || deepMatch(object, source);
};
