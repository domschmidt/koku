module.exports = (config) => {
  config.set({
    frameworks: ['jasmine'],
    customLaunchers: {
      ChromeHeadlessNoGpu: {
        base: 'ChromeHeadless',
        flags: [
          '--no-sandbox',
          '--disable-dev-shm-usage',
          '--disable-gpu',
          '--disable-gpu-compositing',
          '--disable-gpu-shader-disk-cache',
          '--disable-software-rasterizer',
        ],
      },
    },
  });
};
