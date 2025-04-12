import type { Metadata } from "next";
import "./globals.css";
import localFont from "next/font/local";
import { ThemeProvider } from "@/components/theme-provider";

const pretendard = localFont({
  src: "./../node_modules/pretendard/dist/web/variable/woff2/PretendardVariable.woff2",
  display: "swap",
  weight: "45 920",
  variable: "--font-pretendard",
});

export const metadata: Metadata = {
  title: "약국 추천 서비스",
  description: "주소 기준으로 가까운 약국을 찾아드립니다.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <>
      <html
        lang="ko"
        className={`${pretendard.variable}`}
        suppressHydrationWarning
      >
        <head />
        <body className={`${pretendard.className} antialiased min-h-screen`}>
          <ThemeProvider
            attribute="class"
            defaultTheme="system"
            enableSystem
            disableTransitionOnChange
          >
            {children}
          </ThemeProvider>
        </body>
      </html>
    </>
  );
}
