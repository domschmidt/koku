export const get = (obj: any, path: string[] | string, defaultValue?: any) => {
  const pathArray = Array.isArray(path) ? path : path.match(/([^[.\]])+/g);

  if (pathArray) {
    const result = pathArray.reduce((prevObj, key) => prevObj && prevObj[key], obj);
    return result === undefined ? defaultValue : result;
  } else {
    throw new Error('Unable to read path');
  }
};
