"use client";

import React from "react";
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { PharmacyDirection } from "@/lib/api";
import { MapPin, Map, Navigation } from "lucide-react";

interface PharmacyCardProps {
  pharmacy: PharmacyDirection;
}

export function PharmacyCard({ pharmacy }: PharmacyCardProps) {
  return (
    <Card className="h-full">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <MapPin className="h-5 w-5 text-primary" />
          {pharmacy.pharmacyName}
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-2">
        <p className="text-sm text-muted-foreground">
          {pharmacy.pharmacyAddress}
        </p>
        <p className="text-sm font-medium">거리: {pharmacy.distance}</p>
      </CardContent>
      <CardFooter className="flex gap-2">
        <Button
          variant="outline"
          size="sm"
          className="flex-1"
          onClick={() => window.open(pharmacy.directionUrl, "_blank")}
        >
          <Navigation className="h-4 w-4 mr-2" />
          길찾기
        </Button>
        <Button
          variant="outline"
          size="sm"
          className="flex-1"
          onClick={() => window.open(pharmacy.roadViewUrl, "_blank")}
        >
          <Map className="h-4 w-4 mr-2" />
          로드뷰
        </Button>
      </CardFooter>
    </Card>
  );
}
