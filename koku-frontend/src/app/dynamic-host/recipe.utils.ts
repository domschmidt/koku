import { computed, Signal } from '@angular/core';

export const requireRecipeFactory = <TFactory>(
  registry: Readonly<Record<string, TFactory | undefined>>,
  contentType: string,
  domain: string,
): TFactory => {
  const factory = registry[contentType];
  if (!factory) {
    throw new Error(`No ${domain} recipe registered for content type: ${contentType}`);
  }
  return factory;
};

export const createStableRecipe = <TIdentity, TRecipe>(options: {
  identity: () => TIdentity;
  equal: (previous: TIdentity, current: TIdentity) => boolean;
  create: (identity: TIdentity) => TRecipe;
}): Signal<TRecipe> => {
  const identity = computed(options.identity, { equal: options.equal });
  return computed(() => options.create(identity()));
};
