export const set = (obj: any, path: string[] | string, value: any) => {
  const pathArray = Array.isArray(path) ? path : path.match(/([^[.\]])+/g);

  if (pathArray) {
    pathArray.reduce((acc, key, i) => {
      if (acc[key] === undefined) acc[key] = {};
      if (i === pathArray.length - 1) acc[key] = value;
      return acc[key];
    }, obj);
  } else {
    throw new Error('Unable to read path');
  }
};
