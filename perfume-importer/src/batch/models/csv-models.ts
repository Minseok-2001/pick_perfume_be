export interface PerfumeCsv {
  id: string;
  url: string;
  title: string;
  brand_id: string;
  gender: string;
  rating_value: string;
  rating_count: string;
  year: string;
  perfumer1: string;
  perfumer2: string;
  content: string;
  updated_at: string;
  review_sentiment_compound: string;
  review_keyword_top7: string;
}

export interface BrandCsv {
  id: string;
  name: string;
  country: string;
  content?: string;
  founded_year?: string;
  website?: string;
  designer?: string;
  is_luxury?: string;
  is_niche?: string;
  is_popular?: string;
  url?: string;
}

export interface NoteCsv {
  id: string;
  perfume_id: string;
  note_type: string;
  note_name: string;
}

export interface AccordCsv {
  id: string;
  perfume_id: string;
  accord_name: string;
  position: string;
}
