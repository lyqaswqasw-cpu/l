export interface LiveCategory {
  category_id: string;
  category_name: string;
  parent_id: number;
}

export interface LiveStream {
  num: number;
  name: string;
  stream_type: string;
  stream_id: number;
  stream_icon: string;
  category_id: string;
  added?: string;
  custom_sid?: string;
}

export interface UserInfo {
  username: string;
  status: string;
  exp_date: string;
  is_trial: string;
  active_cons: string;
  created_at: string;
  max_connections: string;
}

export interface ServerInfo {
  url: string;
  port: string;
  server_time: string;
}

export interface AccountData {
  user_info?: UserInfo;
  server_info?: ServerInfo;
}
