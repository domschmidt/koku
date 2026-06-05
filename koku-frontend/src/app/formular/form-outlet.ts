export const FORM_OUTLET = {
  CONTENT: 'content',
  PREPEND_OUTER: 'prependOuter',
  PREPEND_INNER: 'prependInner',
  APPEND_INNER: 'appendInner',
  APPEND_OUTER: 'appendOuter',
} as const;

export type FormOutlet = (typeof FORM_OUTLET)[keyof typeof FORM_OUTLET];
