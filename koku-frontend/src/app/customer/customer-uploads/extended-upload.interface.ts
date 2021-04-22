export interface UploadWithProgress extends KokuDto.UploadDto {
  progress?: number;
  errorStatusText?: string;
}
