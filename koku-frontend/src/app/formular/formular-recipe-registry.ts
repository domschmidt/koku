import type { FormularContentRegistry } from './formular.component';

export const assertFormularRecipeCoverage = (formView: KokuDto.FormViewDto, registry: FormularContentRegistry) => {
  for (const content of Object.values(formView.contents ?? {})) {
    if (!registry[content['@type']]) {
      throw new Error(`No recipe registered for content type: ${content['@type']}`);
    }
  }
};
