/**
 * 队伍（对应后端 TeamUserVo）
 */
export type Team = {
    id: number;
    name: string;
    description?: string;
    maxNum: number;
    /** 0 - 公开，1 - 私有，2 - 加密 */
    status: number;
    expireTime?: string | null;
    /** 队长 id */
    userId: number;
    createTime?: string;
    updateTime?: string;
    createUser?: { id?: number; username?: string; avatarUrl?: string | null } | null;
};

/** 列表页 -> 编辑页 传递队伍数据用的 sessionStorage key */
export const TEAM_EDIT_STORAGE_KEY = 'partner-match.team-edit';
