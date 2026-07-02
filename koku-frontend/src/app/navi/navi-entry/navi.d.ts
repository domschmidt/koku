export type Navi = NaviLink | NaviDivider;

export interface NaviLink {
  type: 'link';
  text: string;
  path: string;
  children?: Navi[];
  bottom?: boolean;
  icon?: string;
}

export interface NaviDivider {
  type: 'divider';
  bottom?: boolean;
}
