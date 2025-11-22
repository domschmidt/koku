export interface Navi {
  text: string;
  path: string;
  children?: Navi[];
  bottom?: boolean;
  divider?: 'before' | 'after';
  icon?: string;
}
