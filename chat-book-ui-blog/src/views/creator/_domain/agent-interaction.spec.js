import { describe, expect, it } from 'vitest';
import {
    AGENT_MESSAGE_TYPE,
    buildAnswerRecord,
    buildInteractionResponsePayload,
    buildInteractionResponseSummary,
    isInteractionResponseMessage,
    normalizeInteractiveFormPayload
} from './agent-interaction.js';

const formPayload = {
    formId: 'brief_form',
    title: '补充基础信息',
    description: '回答后我会继续生成建议',
    questions: [
        {
            id: 'audience',
            label: '目标读者',
            type: 'single_choice',
            options: [
                { label: 'Java 开发者', value: 'Java 开发者' },
                { label: '团队技术负责人', value: '团队技术负责人' }
            ]
        },
        {
            id: 'depth',
            label: '解读深度',
            type: 'multi_choice',
            required: false,
            options: [
                { label: '全面解读', value: '全面解读' },
                { label: '只讲核心结论', value: '只讲核心结论' }
            ]
        }
    ]
};

describe('agent-interaction helpers', () => {
    it('normalizes interactive form payload and preserves question options', () => {
        const normalized = normalizeInteractiveFormPayload(JSON.stringify(formPayload));

        expect(normalized.formId).toBe('brief_form');
        expect(normalized.questions).toHaveLength(2);
        expect(normalized.questions[0].type).toBe('single_choice');
        expect(normalized.questions[1].required).toBe(false);
    });

    it('builds interaction response payload and summary for completed answers', () => {
        const answers = {
            audience: 'Java 开发者',
            depth: ['全面解读']
        };

        const payload = buildInteractionResponsePayload(formPayload, answers);
        const summary = buildInteractionResponseSummary(formPayload, answers);

        expect(payload.answers).toEqual([
            {
                questionId: 'audience',
                questionLabel: '目标读者',
                questionType: 'single_choice',
                value: 'Java 开发者'
            },
            {
                questionId: 'depth',
                questionLabel: '解读深度',
                questionType: 'multi_choice',
                value: ['全面解读']
            }
        ]);
        expect(summary).toContain('目标读者：Java 开发者');
        expect(summary).toContain('解读深度：全面解读');
    });

    it('detects interaction response messages and rebuilds answer records', () => {
        const message = {
            role: 'user',
            messageType: AGENT_MESSAGE_TYPE.TEXT,
            payload: {
                interactionResponse: {
                    formId: 'brief_form',
                    answers: [
                        { questionId: 'audience', questionType: 'single_choice', value: 'Java 开发者' },
                        { questionId: 'depth', questionType: 'multi_choice', value: ['全面解读'] }
                    ]
                }
            }
        };

        expect(isInteractionResponseMessage(message)).toBe(true);
        expect(buildAnswerRecord(message.payload.interactionResponse)).toEqual({
            audience: 'Java 开发者',
            depth: ['全面解读']
        });
    });
});
