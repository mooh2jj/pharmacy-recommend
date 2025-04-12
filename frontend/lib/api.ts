import axios, { AxiosError } from "axios";

export interface PharmacyDirection {
  pharmacyName: string;
  pharmacyAddress: string;
  directionUrl: string;
  roadViewUrl: string;
  distance: string;
}

// Next.js API 리디렉션을 사용하므로 상대 경로로 변경
const API_BASE_URL = "";

// axios 인스턴스 생성 및 기본 설정
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: false, // 상대 경로 사용으로 withCredentials 불필요
});

export async function searchPharmaciesByAddress(
  address: string
): Promise<PharmacyDirection[]> {
  try {
    console.log("약국 검색 요청:", address);
    const response = await api.post("/api/direction/search", {
      address,
    });
    console.log("약국 검색 응답:", response);
    return response.data;
  } catch (error: unknown) {
    console.error("약국 조회 오류:", error);
    if (axios.isAxiosError(error)) {
      const axiosError = error as AxiosError;
      if (axiosError.response) {
        // 서버 응답이 있는 경우 (2xx 외의 상태 코드)
        console.error("응답 상태:", axiosError.response.status);
        console.error("응답 데이터:", axiosError.response.data);
      } else if (axiosError.request) {
        // 응답을 받지 못한 경우
        console.error("요청은 전송됐으나 응답 없음:", axiosError.request);
      } else {
        // 요청 설정 중 오류 발생
        console.error("요청 설정 오류:", axiosError.message);
      }
    }
    return [];
  }
}
