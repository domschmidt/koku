export const set = (obj: any, path: string[] | string, value: any) => {
  const pathArray = Array.isArray(path) ? path : path.match(/([^[.\]])+/g);

  if (pathArray) {
    let current = obj;
    const lastIndex = pathArray.length - 1;
    for (const [index, key] of pathArray.entries()) {
      if (index === lastIndex) {
        current[key] = value;
      } else {
        if (current[key] === undefined) {
          current[key] = {};
        }
        current = current[key];
      }
    }
  } else {
    throw new Error('Unable to read path');
  }
};
