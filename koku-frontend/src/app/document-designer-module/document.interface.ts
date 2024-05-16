export interface DocumentBaseRowItem {
  '@type': string;
  id?: number;
  xl?: number;
  lg?: number;
  md?: number;
  sm?: number;
  xs?: number;
}

export interface DocumentBaseRow {
  align?: 'TOP' | 'CENTER' | 'BOTTOM';
  items?: DocumentBaseRowItem[];
}
